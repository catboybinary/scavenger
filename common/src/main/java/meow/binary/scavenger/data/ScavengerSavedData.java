package meow.binary.scavenger.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import meow.binary.scavenger.Modifier;
import meow.binary.scavenger.Scavenger;
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
                    Modifier.CODEC.fieldOf("modifier").forGetter(ScavengerSavedData::getModifier)
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
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public Identifier itemId;
    public Modifier modifier;

    private ScavengerSavedData(Identifier itemId, Modifier modifier) {
        this.itemId = itemId;
        this.modifier = modifier;

        this.setDirty();
    }

    public ScavengerSavedData() {
        this.itemId = Scavenger.TEMP_DATA.item.arch$registryName();
        this.modifier = Scavenger.TEMP_DATA.modifier;

        this.setDirty();
    }

    public static ScavengerSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}
