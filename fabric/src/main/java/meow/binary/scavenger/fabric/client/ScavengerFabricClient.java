package meow.binary.scavenger.fabric.client;

import meow.binary.scavenger.client.ScavengerClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import static meow.binary.scavenger.Scavenger.MOD_ID;

public final class ScavengerFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScavengerClient.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                ScavengerClient.onClientTick(client.level);
            }
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Identifier.fromNamespaceAndPath(MOD_ID, "info"), ScavengerClient::renderHudInfo);
    }
}
