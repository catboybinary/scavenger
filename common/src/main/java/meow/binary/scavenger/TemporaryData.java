package meow.binary.scavenger;

import meow.binary.scavenger.registry.Modifiers;
import meow.binary.scavenger.registry.ScavengerRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class TemporaryData {
    public Item item = Items.AIR;
    public Identifier modifier = Modifiers.NONE.getId();
}
