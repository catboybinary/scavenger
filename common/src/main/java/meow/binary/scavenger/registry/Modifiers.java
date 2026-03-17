package meow.binary.scavenger.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

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

    public static final RegistrySupplier<ScavengerModifier> TURTLE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "turtle"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> SONIC = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "sonic"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> VEGETARIAN = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "vegetarian"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> CARNIVORE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "carnivore"),
            () -> new ScavengerModifier(null, null)
    );

    public static Set<Identifier> getIds() {
        return MODIFIERS.getIds();
    }

    public static ScavengerModifier get(Identifier identifier) {
        return MODIFIERS.get(identifier);
    }

    public static boolean isActive(RegistrySupplier<ScavengerModifier> modifier, Level level) {
        Identifier modifierId = modifier.getId();

        if (level.isClientSide()) {
            return ClientScavengerData.modifier.equals(modifierId);
        } else {
            ServerLevel serverLevel = ((ServerLevel) level).getServer().overworld();
            ScavengerSavedData savedData = ScavengerSavedData.get(serverLevel);
            return savedData.getModifierId().equals(modifierId);
        }
    }

    public static boolean isActive(RegistrySupplier<ScavengerModifier> modifier, ScavengerSavedData savedData) {
        return savedData.getModifierId().equals(modifier.getId());
    }
}
