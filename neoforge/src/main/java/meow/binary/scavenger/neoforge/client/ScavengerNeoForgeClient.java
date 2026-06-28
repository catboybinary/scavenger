package meow.binary.scavenger.neoforge.client;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ScavengerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ClientTickEvent;

@Mod(value = Scavenger.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class ScavengerNeoForgeClient {
    public ScavengerNeoForgeClient(IEventBus modBus, ModContainer container) {
        ScavengerClient.init();

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            var client = Minecraft.getInstance();
            if (client.level != null) {
                ScavengerClient.onClientTick(client.level);
            }
        });
    }

    @SubscribeEvent
    private static void registerGuiLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "info"), ScavengerClient::renderHudInfo);
    }
}
