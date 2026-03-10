package meow.binary.scavenger;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Rarity;

public enum Modifier implements StringRepresentable {
    NONE,
    TWICE,
    THRICE;

    public static final Codec<Modifier> CODEC = StringRepresentable.fromValues(Modifier::values);

    @Override
    public String getSerializedName() {
        return this.name();
    }
}
