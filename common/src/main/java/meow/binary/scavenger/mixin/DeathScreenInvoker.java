package meow.binary.scavenger.mixin;

import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DeathScreen.class)
public interface DeathScreenInvoker {
    @Invoker("exitToTitleScreen")
    void scavenger$exitToTitleScreen();
}
