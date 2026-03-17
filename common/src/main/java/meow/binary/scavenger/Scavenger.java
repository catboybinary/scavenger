package meow.binary.scavenger;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrarManager;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.fabricmc.api.EnvType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class Scavenger {
    public static final RegistrarManager REGISTRIES = RegistrarManager.get(Scavenger.MOD_ID);
    public static final TemporaryData TEMP_DATA = new TemporaryData();
    public static final String MOD_ID = "scavenger";

    public static void init() {
        TickEvent.PLAYER_POST.register(player -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ServerLevel serverLevel = serverPlayer.level().getServer().overworld();
            ScavengerSavedData data = ScavengerSavedData.get(serverLevel);
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
            SyncScavengerDataPacket packet = new SyncScavengerDataPacket(data.getItem(), data.getModifierId());
            NetworkManager.sendToPlayer(serverPlayer, packet);
        });

        ShatterLibNetwork.registerS2CPayloadType(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC);
    }

    private static void checkWinCondition(ServerPlayer player, ScavengerSavedData data) {
        int itemCount = 1;
        if (Modifiers.isActive(Modifiers.TWICE, data)) itemCount = 2;
        else if (Modifiers.isActive(Modifiers.THRICE, data)) itemCount = 3;

        boolean hasWon = player.getInventory().countItem(data.getItem()) >= itemCount;
        if (hasWon) {
            data.win();
            player.sendSystemMessage(Component.literal("Congratulations, you have won!").withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
