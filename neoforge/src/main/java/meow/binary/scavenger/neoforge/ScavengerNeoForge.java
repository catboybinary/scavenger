package meow.binary.scavenger.neoforge;

import meow.binary.scavenger.Scavenger;
import net.neoforged.fml.common.Mod;

@Mod(Scavenger.MOD_ID)
public final class ScavengerNeoForge {
    public ScavengerNeoForge() {
        // Run our common setup.
        Scavenger.init();
    }
}
