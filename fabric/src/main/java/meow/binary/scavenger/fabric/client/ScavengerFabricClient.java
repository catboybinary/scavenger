package meow.binary.scavenger.fabric.client;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ScavengerClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class ScavengerFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScavengerClient.init();
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "info"), ScavengerClient::renderHudInfo);
    }
}
