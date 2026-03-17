package meow.binary.scavenger.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class ScavengerSavedData extends SavedData {
    private static final String DATA_NAME = "scavenger_data";
    public static final Codec<ScavengerSavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("itemId").forGetter(ScavengerSavedData::getItemId),
                    Identifier.CODEC.fieldOf("modifier").forGetter(ScavengerSavedData::getModifierId),
                    Codec.BOOL.fieldOf("hasWon").forGetter(ScavengerSavedData::hasWon)
            ).apply(instance, ScavengerSavedData::new));

    public static final SavedDataType<ScavengerSavedData> TYPE = new SavedDataType<>(
            DATA_NAME,
            ScavengerSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public Identifier getItemId() {
        return itemId;
    }

    public Item getItem() {
        return BuiltInRegistries.ITEM.getValue(itemId);
    }

    public void setItem(Item item) {
        this.itemId = item.arch$registryName();

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

    public void win() {
        this.hasWon = true;

        this.setDirty();
    }

    private Identifier itemId;
    private Identifier modifierId;
    private boolean hasWon;

    private ScavengerSavedData(Identifier itemId, Identifier modifierId, boolean hasWon) {
        this.itemId = itemId;
        this.modifierId = modifierId;
        this.hasWon = hasWon;

        this.setDirty();
    }

    public ScavengerSavedData() {
        this.itemId = Scavenger.TEMP_DATA.item.arch$registryName();
        this.modifierId = Scavenger.TEMP_DATA.modifier;
        this.hasWon = false;

        Scavenger.TEMP_DATA.item = Items.AIR;
        Scavenger.TEMP_DATA.modifier = Modifiers.NONE.getId();

        this.setDirty();
    }

    public static ScavengerSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}
