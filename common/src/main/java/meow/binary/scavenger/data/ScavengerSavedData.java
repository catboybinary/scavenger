package meow.binary.scavenger.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScavengerSavedData extends SavedData {
    private static final String DATA_NAME = "scavenger_data";
    private static final int CURRENT_DATA_VERSION = 1;

    private static final List<PreLoadMigration> PRE_LOAD_MIGRATIONS = List.of(
            ScavengerSavedData::migrate_v0_to_v1
    );

    public static final Codec<ScavengerSavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("dataVersion", 0).forGetter(ScavengerSavedData::getDataVersion),
                    Identifier.CODEC.fieldOf("itemId").forGetter(ScavengerSavedData::getItemId),
                    Identifier.CODEC.fieldOf("modifier").forGetter(ScavengerSavedData::getModifierId),
                    Codec.BOOL.fieldOf("hasWon").forGetter(ScavengerSavedData::hasWon),
                    Codec.LONG.fieldOf("winTimestamp").forGetter(ScavengerSavedData::getWinTimestamp)
            ).apply(instance, (dataVersion, itemId, modifier, hasWon, winTimestamp) -> {
                var data = new ScavengerSavedData(itemId, modifier, hasWon, winTimestamp);
                data.dataVersion = dataVersion;
                return data;
            }));

    public static final SavedDataType<ScavengerSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, DATA_NAME),
            ScavengerSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public int getDataVersion() {
        return dataVersion;
    }

    public Identifier getItemId() {
        return itemId;
    }
    public Item getItem() {
        return BuiltInRegistries.ITEM.getValue(itemId);
    }

    public void setItem(Item item) {
        this.itemId = BuiltInRegistries.ITEM.getKey(item);

        this.setDirty();
    }

    public long getWinTimestamp() {
        return winTimestamp;
    }

    public void setWinTimestamp(long winTimestamp) {
        this.winTimestamp = winTimestamp;
        this.setDirty();
    }

    public Identifier getModifierId() {
        return modifierId;
    }

    public ScavengerModifier getModifier() {
        return Modifiers.get(modifierId);
    }

    public void setModifierId(Identifier modifierId) {
        this.modifierId = modifierId;

        this.setDirty();
    }

    public boolean hasWon() {
        return hasWon;
    }

    public void win(long winTimestamp) {
        this.hasWon = true;
        this.setWinTimestamp(winTimestamp);
        this.setDirty();
    }

    private int dataVersion;
    private Identifier itemId;
    private Identifier modifierId;
    private boolean hasWon;
    private long winTimestamp;

    private ScavengerSavedData(Identifier itemId, Identifier modifierId, boolean hasWon, long winTimestamp) {
        this.itemId = itemId;
        this.modifierId = modifierId;
        this.hasWon = hasWon;
        this.winTimestamp = winTimestamp;

        this.setDirty();
    }

    public ScavengerSavedData() {
        this.dataVersion = CURRENT_DATA_VERSION;
        this.itemId = BuiltInRegistries.ITEM.getKey(Scavenger.TEMP_DATA.item);
        this.modifierId = Scavenger.TEMP_DATA.modifier;
        this.hasWon = false;

        Scavenger.TEMP_DATA.item = Items.AIR;
        Scavenger.TEMP_DATA.modifier = Modifiers.NONE.getId();

        this.setDirty();
    }

    public static ScavengerSavedData get(MinecraftServer server) {
        applyPreLoadMigrations(server);
        ScavengerSavedData data = server.getDataStorage().computeIfAbsent(TYPE);
        data.applyDataMigrations(server);
        return data;
    }

    private static void applyPreLoadMigrations(MinecraftServer server) {
        for (var migration : PRE_LOAD_MIGRATIONS) {
            migration.apply(server);
        }
    }

    private void applyDataMigrations(MinecraftServer server) {
        if (dataVersion >= CURRENT_DATA_VERSION) return;
        while (dataVersion < CURRENT_DATA_VERSION) {
            applyDataMigration(dataVersion, server);
            dataVersion++;
        }
        this.setDirty();
    }

    private void applyDataMigration(int fromVersion, MinecraftServer server) {
        // Add future data migrations here keyed by fromVersion
    }

    private static void migrate_v0_to_v1(MinecraftServer server) {
        Path oldFile = server.getWorldPath(LevelResource.ROOT).resolve("data/scavenger_data.dat");
        Path newDir = server.getWorldPath(LevelResource.DATA).resolve(Scavenger.MOD_ID);
        Path newFile = newDir.resolve("scavenger_data.dat");

        if (!Files.exists(oldFile)) {
            return;
        }

        try {
            Files.createDirectories(newDir);
            Files.move(oldFile, newFile);
        } catch (IOException ignored) {
        }
    }

    @FunctionalInterface
    private interface PreLoadMigration {
        void apply(MinecraftServer server);
    }

    public boolean isEmpty() {
        return this.modifierId.equals(Modifiers.NONE.getId()) && itemId.equals(BuiltInRegistries.ITEM.getKey(Items.AIR));
    }
}
