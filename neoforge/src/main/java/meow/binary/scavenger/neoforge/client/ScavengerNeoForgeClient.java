package meow.binary.scavenger.neoforge.client;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ScavengerClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@Mod(value = Scavenger.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class ScavengerNeoForgeClient {
    public ScavengerNeoForgeClient(IEventBus modBus, ModContainer container) {
        ScavengerClient.init();
    }
}