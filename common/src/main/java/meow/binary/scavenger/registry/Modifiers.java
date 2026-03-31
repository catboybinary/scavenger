package meow.binary.scavenger.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

    public static final RegistrySupplier<ScavengerModifier> SPEED_UP = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "speed_up"),
            () -> new ScavengerModifier(null, level -> level.tickRateManager().setTickRate(40))
    );

    public static final RegistrySupplier<ScavengerModifier> MOLE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "mole"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> DRUNK = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "drunk"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> ASOCIAL = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "asocial"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> MAIN_CHARACTER = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "main_character"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> NPC = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "npc"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> BEDROCK = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "bedrock"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> SNAIL = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "snail"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> HOLEY_POCKETS = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "holey_pockets"),
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

    public static MutableComponent getName(Identifier modifierId) {
        return Component.translatable("scavenger.modifier." + modifierId.getPath());
    }

    public static MutableComponent getDescription(Identifier modifierId) {
        return Component.translatable("scavenger.modifier." + modifierId.getPath() + ".description");
    }
}
