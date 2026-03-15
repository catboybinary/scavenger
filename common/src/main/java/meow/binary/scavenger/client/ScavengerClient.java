package meow.binary.scavenger.client;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Mod;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;

public final class ScavengerClient {
    public static void init() {
        //System.out.println("test!!");
        ShatterLibNetwork.registerS2CReceiver(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC, (syncScavengerDataPacket, packetContext) -> {
            ClientScavengerData.item = syncScavengerDataPacket.getItem();
            ClientScavengerData.modifier = syncScavengerDataPacket.getModifier();
        });

        TickEvent.PLAYER_POST.register(player -> {
            if (!player.level().isClientSide()) {
                return;
            }

            if (ClientScavengerData.modifier.equals(Modifiers.TURTLE.getId())) {
                Minecraft.getInstance().options.sensitivity().set(0d);
            } else if (ClientScavengerData.modifier.equals(Modifiers.SONIC.getId())) {
                Minecraft.getInstance().options.sensitivity().set(1d);
            } else {
                Minecraft.getInstance().options.sensitivity().set(0.5d);
            }
        });
    }
}
