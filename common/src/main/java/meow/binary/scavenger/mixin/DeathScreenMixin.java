package meow.binary.scavenger.mixin;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {
    @Shadow
    private boolean hardcore;

    @Unique
    private Button scavenger$restartButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void scavenger$addRestartButton(CallbackInfo ci) {
        if (!hardcore || ClientScavengerData.isEmpty()) {
            return;
        }

        Screen screen = (Screen) (Object) this;
        this.scavenger$restartButton = Button.builder(Component.translatable("scavenger.restart_run"), button -> {
                    ScavengerWorldCreateScreen.queueRestart(Minecraft.getInstance(), ClientScavengerData.item, ClientScavengerData.modifier);
                    ((DeathScreenInvoker) this).scavenger$exitToTitleScreen();
                })
                .bounds(screen.width / 2 - 100, screen.height / 4 + 120, 200, 20)
                .build();
        this.scavenger$restartButton.active = false;
        screen.addRenderableWidget(this.scavenger$restartButton);
    }

    @Inject(method = "setButtonsActive", at = @At("TAIL"))
    private void scavenger$syncRestartButton(boolean active, CallbackInfo ci) {
        if (this.scavenger$restartButton != null) {
            this.scavenger$restartButton.active = active;
        }
    }
}
