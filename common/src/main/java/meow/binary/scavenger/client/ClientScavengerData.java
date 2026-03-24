package meow.binary.scavenger.client;

import dev.architectury.registry.registries.RegistrySupplier;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ClientScavengerData {
    public static Item item = Items.AIR;
    public static Identifier modifier = Modifiers.NONE.getId();

    public static boolean isEmpty() {
        return modifier.equals(Modifiers.NONE.getId()) && item.equals(Items.AIR);
    }

    public static boolean is(RegistrySupplier<ScavengerModifier> otherModifier) {
        return modifier.equals(otherModifier.getId());
    }
}
