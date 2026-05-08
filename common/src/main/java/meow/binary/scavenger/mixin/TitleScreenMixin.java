package meow.binary.scavenger.mixin;

import meow.binary.scavenger.client.ScavengerClient;
import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Unique
    private boolean scavenger$queuedRestartLaunch;

    @Inject(method = "init", at = @At("TAIL"))
    private void scavenger$launchPendingRestart(CallbackInfo ci) {
        ScavengerClient.onTitleScreenShown();

        if (scavenger$queuedRestartLaunch || !ScavengerWorldCreateScreen.hasPendingRestart()) {
            return;
        }

        scavenger$queuedRestartLaunch = true;
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.submit(() -> ScavengerWorldCreateScreen.launchPendingRestart(minecraft));
    }
}
