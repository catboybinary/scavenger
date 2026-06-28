package meow.binary.scavenger.neoforge;

import meow.binary.scavenger.Scavenger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerPlayerTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

@Mod(Scavenger.MOD_ID)
public final class ScavengerNeoForge {
    public ScavengerNeoForge(IEventBus modBus) {
        Scavenger.init();

        Scavenger.packetSender = (player, packet) ->
                player.connection.send(packet);

        var bus = NeoForge.EVENT_BUS;

        bus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                Scavenger.onPlayerJoin(player);
            }
        });

        bus.addListener(ServerPlayerTickEvent.Post.class, event -> {
            Scavenger.onPlayerTick(event.getPlayer());
        });

        bus.addListener(LevelEvent.Load.class, event -> {
            if (event.getLevel() instanceof ServerLevel serverLevel
                    && serverLevel.dimension() == Level.OVERWORLD) {
                Scavenger.onServerLevelLoad(serverLevel);
            }
        });
    }
}
