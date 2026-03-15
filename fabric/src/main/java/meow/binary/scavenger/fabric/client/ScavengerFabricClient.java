package meow.binary.scavenger.fabric.client;

import meow.binary.scavenger.client.ScavengerClient;
import net.fabricmc.api.ClientModInitializer;

public final class ScavengerFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScavengerClient.init();
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }
}
