package meow.binary.scavenger.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Set;

import static meow.binary.scavenger.registry.ScavengerRegistries.MODIFIERS;

public class Modifiers {
    public static final RegistrySupplier<ScavengerModifier> NONE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "none"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> TWICE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "twice"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> THRICE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "thrice"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> GIANT = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "giant"),
            () -> new ScavengerModifier(player -> player.getAttribute(Attributes.SCALE).setBaseValue(2), null)
    );

    public static final RegistrySupplier<ScavengerModifier> TINY = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "tiny"),
            () -> new ScavengerModifier(player -> player.getAttribute(Attributes.SCALE).setBaseValue(0.5), null)
    );

    public static Set<Identifier> getIds() {
        return MODIFIERS.getIds();
    }

    public static ScavengerModifier get(Identifier identifier) {
        return MODIFIERS.get(identifier);
    }
}
