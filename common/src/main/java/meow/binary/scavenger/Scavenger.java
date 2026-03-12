package meow.binary.scavenger;

import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.registries.RegistrarManager;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class Scavenger {
    public static final RegistrarManager REGISTRIES = RegistrarManager.get(Scavenger.MOD_ID);
    public static final TemporaryData TEMP_DATA = new TemporaryData();
    public static final String MOD_ID = "scavenger";

    public static void init() {
        TickEvent.PLAYER_POST.register(player -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ServerLevel serverLevel = serverPlayer.level();
            ScavengerSavedData data = ScavengerSavedData.get(serverLevel);
            ScavengerModifier modifier = data.getModifier();
            if (!modifier.hasPlayerTick()) {
                return;
            }

            modifier.playerTick(serverPlayer);
        });
    }
}
