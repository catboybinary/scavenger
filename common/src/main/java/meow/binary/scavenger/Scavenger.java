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
import net.fabricmc.api.EnvType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class Scavenger {
    public static final RegistrarManager REGISTRIES = RegistrarManager.get(Scavenger.MOD_ID);
    public static final TemporaryData TEMP_DATA = new TemporaryData();
    public static final String MOD_ID = "scavenger";

    public static void init() {
//        if (Platform.getEnv() == EnvType.CLIENT) {
//            ScavengerClient.init();
//        }

        TickEvent.PLAYER_POST.register(player -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ServerLevel serverLevel = serverPlayer.level().getServer().overworld();
            ScavengerSavedData data = ScavengerSavedData.get(serverLevel);
            ScavengerModifier modifier = data.getModifier();
            if (!modifier.hasPlayerTick()) {
                return;
            }

            modifier.playerTick(serverPlayer);
        });

        PlayerEvent.PLAYER_JOIN.register(serverPlayer -> {
            ServerLevel serverLevel = serverPlayer.level().getServer().overworld();
            ScavengerSavedData data = ScavengerSavedData.get(serverLevel);
            SyncScavengerDataPacket packet = new SyncScavengerDataPacket(data.getItem(), data.getModifierId());
            NetworkManager.sendToPlayer(serverPlayer, packet);
        });

        ShatterLibNetwork.registerS2CPayloadType(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC);
    }
}
