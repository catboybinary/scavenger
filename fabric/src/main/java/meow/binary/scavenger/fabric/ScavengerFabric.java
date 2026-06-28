package meow.binary.scavenger.fabric;

import it.hurts.shatterbyte.shatterlib.fabric.FabricPlatformHelper;
import it.hurts.shatterbyte.shatterlib.platform.ShatterLibServices;
import meow.binary.scavenger.Scavenger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public final class ScavengerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ShatterLibServices.initialize(FabricPlatformHelper.INSTANCE);
        Scavenger.init();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Scavenger.onPlayerJoin((ServerPlayer) handler.getPlayer());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                Scavenger.onPlayerTick(player);
            }
        });

        ServerLevelEvents.LOAD.register((server, world) -> {
            Scavenger.onServerLevelLoad(world);
        });
    }
}
