package meow.binary.scavenger.fabric;

import meow.binary.scavenger.Scavenger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class ScavengerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Scavenger.init();

        Scavenger.packetSender = (player, packet) ->
                ServerPlayNetworking.send(player, packet);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Scavenger.onPlayerJoin((ServerPlayer) handler.getPlayer());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                Scavenger.onPlayerTick(player);
            }
        });

        ServerWorldEvents.LOAD.register((server, world) -> {
            Scavenger.onServerLevelLoad(world);
        });
    }
}
