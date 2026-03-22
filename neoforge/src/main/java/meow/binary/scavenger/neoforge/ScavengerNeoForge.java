package meow.binary.scavenger.neoforge;

import meow.binary.scavenger.Scavenger;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

@Mod(Scavenger.MOD_ID)
public final class ScavengerNeoForge {
    public ScavengerNeoForge() {
        // Run our common setup.
        Scavenger.init();
    }
}
