package meow.binary.scavenger.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import net.minecraft.client.renderer.item.properties.numeric.Damage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.*;

import static meow.binary.scavenger.registry.ScavengerRegistries.MODIFIERS;

public class Modifiers {
    private static final Random RANDOM = new Random();

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

//    public static final RegistrySupplier<ScavengerModifier> BEDROCK = MODIFIERS.register(
//            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "bedrock"),
//            () -> new ScavengerModifier(null, null)
//    );

    public static final RegistrySupplier<ScavengerModifier> SNAIL = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "snail"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> HOLEY_POCKETS = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "holey_pockets"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> BRITTLE_BONES = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "brittle_bones"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> ONE_ARM = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "one_arm"),
            () -> new ScavengerModifier(player -> {
                Inventory inventory = player.getInventory();
                ItemStack offHandItem = inventory.getItem(Inventory.SLOT_OFFHAND);
                if (!offHandItem.isEmpty()) {
                    player.drop(offHandItem, false, true);
                    inventory.setItem(Inventory.SLOT_OFFHAND, ItemStack.EMPTY);
                }
            }, null)
    );

    public static final RegistrySupplier<ScavengerModifier> HYDROPHOBIC = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "hydrophobic"),
            () -> new ScavengerModifier(player -> {
                if (player.isInWaterOrRain() && !player.isDeadOrDying()) {
                    ServerLevel level = player.level();
                    player.hurtServer(level, level.damageSources().magic(), 9999);
                }
            }, null)
    );

    public static final RegistrySupplier<ScavengerModifier> SILENCE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "silence"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> INSOMNIA = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "insomnia"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> UNEDUCATED = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "uneducated"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> DEJAVU = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "dejavu"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> LARGE_BIOMES = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "large_biomes"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> AMPLIFIED = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "amplified"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> NOIR = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "noir"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> ECLIPSE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "eclipse"),
            () -> new ScavengerModifier(null, level -> {
                level.getGameRules().set(GameRules.ADVANCE_TIME, false, level.getServer());
                level.setDayTime(18000);
            })
    );

    public static final RegistrySupplier<ScavengerModifier> SOLSTICE = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "solstice"),
            () -> new ScavengerModifier(null, level -> {
                level.getGameRules().set(GameRules.ADVANCE_TIME, false, level.getServer());
                level.setDayTime(6000);
            })
    );

    public static final RegistrySupplier<ScavengerModifier> ALIEN = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "alien"),
            () -> new ScavengerModifier(player -> player.getAttribute(Attributes.GRAVITY).setBaseValue(0.04), null)
    );

    public static final RegistrySupplier<ScavengerModifier> TOURIST = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "tourist"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> FEARFUL = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "fearful"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> SHUFFLED_CHESTS = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "shuffled_chests"),
            () -> new ScavengerModifier(null, null)
    );

    public static final RegistrySupplier<ScavengerModifier> FINALIST = MODIFIERS.register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "finalist"),
            () -> new ScavengerModifier(null, null)
    );

//    public static final RegistrySupplier<ScavengerModifier> BAD_ALCHEMIST = MODIFIERS.register(
//            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "bad_alchemist"),
//            () -> new ScavengerModifier(serverPlayer -> {
//                if (serverPlayer.getActiveEffects().isEmpty()) {
//                    List<MobEffect> effects = BuiltInRegistries.MOB_EFFECT.stream()
//                            .filter(effect -> !effect.isInstantenous())
//                            .toList();
//                    if (effects.isEmpty()) {
//                        return;
//                    }
//
//                    MobEffect effect = effects.get(RANDOM.nextInt(effects.size()));
//                    serverPlayer.addEffect(new MobEffectInstance(effect, 20 * 60 * 5, ));
//                }
//            }, null)
//    );

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
