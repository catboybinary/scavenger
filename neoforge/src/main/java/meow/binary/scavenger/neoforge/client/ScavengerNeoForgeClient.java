package meow.binary.scavenger.neoforge.client;

import dev.architectury.event.events.client.ClientGuiEvent;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ScavengerClient;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@Mod(value = Scavenger.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class ScavengerNeoForgeClient {
    public ScavengerNeoForgeClient(IEventBus modBus, ModContainer container) {
        ScavengerClient.init();
    }

    @SubscribeEvent
    private static void registerGuiLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "info"), ScavengerClient::renderHudInfo);
    }
}