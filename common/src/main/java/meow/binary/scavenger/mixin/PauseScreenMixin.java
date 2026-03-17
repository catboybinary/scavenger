package meow.binary.scavenger.mixin;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.client.screen.VictoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void renderInfo(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, ClientScavengerData.item.getName(), 8, 8, 0xffffffff, true);
        guiGraphics.drawString(font, ClientScavengerData.modifier.getPath(), 8, 18, 0xffffffff, true);

        if (Minecraft.getInstance().hasShiftDown()) {
            Minecraft.getInstance().setScreen(new VictoryScreen());
        }
    }
}
