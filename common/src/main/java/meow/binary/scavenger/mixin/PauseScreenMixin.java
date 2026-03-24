package meow.binary.scavenger.mixin;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.client.screen.VictoryScreen;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void renderInfo(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ClientScavengerData.isEmpty()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        Component modifierName = Modifiers.getName(ClientScavengerData.modifier).withStyle(ChatFormatting.BOLD);
        Component itemName = ClientScavengerData.item.getName().copy().withStyle(ChatFormatting.BOLD);
        Component activeModifier = Component.translatable("scavenger.active_modifier").append(": ").append(modifierName);
        Component itemToFind = Component.translatable("scavenger.item_to_find").append(": ").append(itemName);
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(width/2f, height - 32);
        guiGraphics.drawString(font, activeModifier, - font.width(activeModifier) / 2, 8, 0xffffffff, true);
        guiGraphics.drawString(font, itemToFind, - font.width(itemToFind) / 2, 18, 0xffffffff, true);
        //guiGraphics.drawString(font, modifierName, - font.width(modifierName) / 2, 18, 0xffffffff, true);
        //guiGraphics.drawString(font, itemName, - font.width(itemName) / 2, 48, 0xffffffff, true);
        guiGraphics.pose().popMatrix();

        if (Minecraft.getInstance().hasShiftDown()) {
            Minecraft.getInstance().setScreen(new VictoryScreen());
        }
    }
}
