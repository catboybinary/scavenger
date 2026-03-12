package meow.binary.scavenger.registry;

import dev.architectury.registry.registries.Registrar;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import net.minecraft.resources.Identifier;

public class ScavengerRegistries {
    protected static final Registrar<ScavengerModifier> MODIFIERS = Scavenger.REGISTRIES.builder(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "modifiers"),
            new ScavengerModifier[0]
    ).build();
}