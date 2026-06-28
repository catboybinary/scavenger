package meow.binary.scavenger.network;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface PacketSender {
    void send(ServerPlayer player, SyncScavengerDataPacket packet);
}
