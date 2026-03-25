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
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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

        int modifierWidth = font.width(activeModifier);
        int itemWidth = font.width(itemToFind);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(width/2f, height - 32);
        guiGraphics.drawString(font, activeModifier, - modifierWidth / 2, 8, 0xffffffff, true);
        guiGraphics.drawString(font, itemToFind, - itemWidth / 2, 18, 0xffffffff, true);
        //guiGraphics.drawString(font, modifierName, - font.width(modifierName) / 2, 18, 0xffffffff, true);
        //guiGraphics.drawString(font, itemName, - font.width(itemName) / 2, 48, 0xffffffff, true);
        guiGraphics.pose().popMatrix();

        float modifierPos = (width - modifierWidth) / 2f;
        float itemPos = (width - itemWidth) / 2f;
        int yPos = height - 32;

        if (mouseX >= modifierPos && mouseX < modifierPos + modifierWidth && mouseY >= yPos + 8 && mouseY < yPos + 17) {
            guiGraphics.renderTooltip(font, List.of(ClientTooltipComponent.create(Modifiers.getDescription(ClientScavengerData.modifier).getVisualOrderText())),
                    mouseX, mouseY, DefaultTooltipPositioner.INSTANCE,null
            );
        } else if (mouseX >= itemPos && mouseX < itemPos + itemWidth && mouseY >= yPos + 18 && mouseY < yPos + 27) {
            guiGraphics.setTooltipForNextFrame(font, ClientScavengerData.item.getDefaultInstance(), mouseX, mouseY);
        }

        if (Minecraft.getInstance().hasShiftDown()) {
            Minecraft.getInstance().setScreen(new VictoryScreen());
        }
    }
}
