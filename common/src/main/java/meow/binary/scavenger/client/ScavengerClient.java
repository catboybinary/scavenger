package meow.binary.scavenger.client;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Mod;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public final class ScavengerClient {
    public static void init() {
        //System.out.println("test!!");
        ShatterLibNetwork.registerS2CReceiver(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC, (syncScavengerDataPacket, packetContext) -> {
            ClientScavengerData.item = syncScavengerDataPacket.getItem();
            ClientScavengerData.modifier = syncScavengerDataPacket.getModifier();
        });

        TickEvent.PLAYER_POST.register(player -> {
            Level level = player.level();
            if (!level.isClientSide()) {
                return;
            }

            if (Modifiers.isActive(Modifiers.TURTLE, level)) {
                Minecraft.getInstance().options.sensitivity().set(0d);
            } else if (Modifiers.isActive(Modifiers.SONIC, level)) {
                Minecraft.getInstance().options.sensitivity().set(1d);
            } else {
                Minecraft.getInstance().options.sensitivity().set(0.5d);
            }
        });
    }
}
