package meow.binary.scavenger.registry;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import meow.binary.scavenger.mixin.ServerLevelAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.Set;

import static meow.binary.scavenger.registry.ScavengerRegistries.register;

public class Modifiers {
    public static final ScavengerModifier NONE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "none"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier TWICE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "twice"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier THRICE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "thrice"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier GIANT = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "giant"),
            new ScavengerModifier(player -> player.getAttribute(Attributes.SCALE).setBaseValue(2), null)
    );

    public static final ScavengerModifier TINY = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "tiny"),
            new ScavengerModifier(player -> player.getAttribute(Attributes.SCALE).setBaseValue(0.5), null)
    );

    public static final ScavengerModifier TURTLE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "turtle"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier SONIC = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "sonic"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier VEGETARIAN = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "vegetarian"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier CARNIVORE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "carnivore"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier SPEED_UP = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "speed_up"),
            new ScavengerModifier(null, level -> level.tickRateManager().setTickRate(40))
    );

    public static final ScavengerModifier MOLE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "mole"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier DRUNK = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "drunk"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier ASOCIAL = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "asocial"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier MAIN_CHARACTER = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "main_character"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier NPC = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "npc"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier SNAIL = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "snail"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier HOLEY_POCKETS = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "holey_pockets"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier BRITTLE_BONES = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "brittle_bones"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier ONE_ARM = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "one_arm"),
            new ScavengerModifier(player -> {
                Inventory inventory = player.getInventory();
                ItemStack offHandItem = inventory.getItem(Inventory.SLOT_OFFHAND);
                if (!offHandItem.isEmpty()) {
                    player.drop(offHandItem, false, true);
                    inventory.setItem(Inventory.SLOT_OFFHAND, ItemStack.EMPTY);
                }
            }, null)
    );

    public static final ScavengerModifier HYDROPHOBIC = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "hydrophobic"),
            new ScavengerModifier(player -> {
                if (player.isInWaterOrRain() && !player.isDeadOrDying()) {
                    ServerLevel level = (ServerLevel) player.level();
                    player.hurtServer(level, level.damageSources().magic(), 9999);
                }
            }, null)
    );

    public static final ScavengerModifier SILENCE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "silence"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier INSOMNIA = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "insomnia"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier UNEDUCATED = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "uneducated"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier DEJAVU = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "dejavu"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier LARGE_BIOMES = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "large_biomes"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier AMPLIFIED = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "amplified"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier NOIR = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "noir"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier ECLIPSE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "eclipse"),
            new ScavengerModifier(null, level -> {
                level.getGameRules().set(GameRules.ADVANCE_TIME, false, ((ServerLevelAccessor) level).scavenger$getServer());
            })
    );

    public static final ScavengerModifier SOLSTICE = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "solstice"),
            new ScavengerModifier(null, level -> {
                level.getGameRules().set(GameRules.ADVANCE_TIME, false, ((ServerLevelAccessor) level).scavenger$getServer());
            })
    );

    public static final ScavengerModifier ALIEN = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "alien"),
            new ScavengerModifier(player -> player.getAttribute(Attributes.GRAVITY).setBaseValue(0.04), null)
    );

    public static final ScavengerModifier TOURIST = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "tourist"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier FEARFUL = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "fearful"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier SHUFFLED_CHESTS = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "shuffled_chests"),
            new ScavengerModifier(null, null)
    );

    public static final ScavengerModifier FINALIST = register(
            Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "finalist"),
            new ScavengerModifier(null, null)
    );

    public static Set<Identifier> getIds() {
        return ScavengerRegistries.getIds();
    }

    public static ScavengerModifier get(Identifier identifier) {
        return ScavengerRegistries.get(identifier);
    }

    public static boolean isActive(ScavengerModifier modifier, Level level) {
        Identifier modifierId = modifier.getId();

        if (level.isClientSide()) {
            return ClientScavengerData.modifier.equals(modifierId);
        } else {
            ScavengerSavedData savedData = ScavengerSavedData.get(((ServerLevelAccessor) level).scavenger$getServer());
            return savedData.getModifierId().equals(modifierId);
        }
    }

    public static boolean isActive(ScavengerModifier modifier, ScavengerSavedData savedData) {
        return savedData.getModifierId().equals(modifier.getId());
    }

    public static MutableComponent getName(Identifier modifierId) {
        return Component.translatable("scavenger.modifier." + modifierId.getPath());
    }

    public static MutableComponent getDescription(Identifier modifierId) {
        return Component.translatable("scavenger.modifier." + modifierId.getPath() + ".description");
    }
}
