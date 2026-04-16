package meow.binary.scavenger;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrarManager;
import it.hurts.shatterbyte.shatterlib.module.config.ConfigManager;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import meow.binary.scavenger.client.Config;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class Scavenger {
    public static final Config CONFIG = new Config();
    public static final RegistrarManager REGISTRIES = RegistrarManager.get(Scavenger.MOD_ID);
    public static final TemporaryData TEMP_DATA = new TemporaryData();
    public static final String MOD_ID = "scavenger";

    public static final Player.BedSleepingProblem INSOMNIA_PROBLEM = new Player.BedSleepingProblem(Component.translatable("scavenger.insomnia"));

    public static final TagKey<Item> VEGETARIAN_FOOD = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "vegetarian_food"));

    public static void init() {
        ConfigManager.registerConfig("scavenger", CONFIG);

        TickEvent.PLAYER_POST.register(player -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ServerLevel serverLevel = serverPlayer.level().getServer().overworld();
            ScavengerSavedData data = ScavengerSavedData.get(serverLevel);
            if (data.isEmpty()) {
                return;
            }

            ScavengerModifier modifier = data.getModifier();

            if (serverPlayer.tickCount % 10 == 0 && !data.hasWon()) {
                Scavenger.checkWinCondition(serverPlayer, data);
            }

            if (modifier.hasPlayerTick()) {
                modifier.playerTick(serverPlayer);
            }
        });

        PlayerEvent.PLAYER_JOIN.register(serverPlayer -> {
            ServerLevel serverLevel = serverPlayer.level().getServer().overworld();
            ScavengerSavedData data = ScavengerSavedData.get(serverLevel);
            SyncScavengerDataPacket packet = new SyncScavengerDataPacket(data.getItem(), data.getModifierId(), data.getWinTimestamp());
            NetworkManager.sendToPlayer(serverPlayer, packet);
        });

        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            ScavengerSavedData data = ScavengerSavedData.get(level.getServer().overworld());
            if (data.isEmpty()) {
                return;
            }

            ScavengerModifier modifier = data.getModifier();

            if (modifier.hasWorldStart()) {
                modifier.onWorldStart(level);
            }
        });

        ShatterLibNetwork.registerS2CPayloadType(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC);
    }

    private static void checkWinCondition(ServerPlayer player, ScavengerSavedData data) {
        int itemCount = getItemCount(data.getModifierId());

        boolean hasWon = player.getInventory().countItem(data.getItem()) >= itemCount;
        if (hasWon) {
            data.win(player.level().getGameTime());
            SyncScavengerDataPacket packet = new SyncScavengerDataPacket(data.getItem(), data.getModifierId(), data.getWinTimestamp());
            NetworkManager.sendToPlayer(player, packet);
            player.sendSystemMessage(Component.literal("Congratulations, you have won!").withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    public static int getItemCount(Identifier modifier) {
        int itemCount = 1;
        if (modifier.equals(Modifiers.TWICE.getId())) itemCount = 2;
        else if (modifier.equals(Modifiers.THRICE.getId())) itemCount = 3;

        return itemCount;
    }

    public static boolean isSlotBlocked(int index, Level level) {
        if (Modifiers.isActive(Modifiers.HOLEY_POCKETS, level) && index > 8 && index < 36) {
            return true;
        }

//        if (Modifiers.isActive(Modifiers.BRITTLE_BONES, level) && index >= 36 && index < 40) {
//            return true;
//        }

//        if (Modifiers.isActive(Modifiers.ONE_ARM, level) && index == 40) {
//            return true;
//        }

        return false;
    }
}
