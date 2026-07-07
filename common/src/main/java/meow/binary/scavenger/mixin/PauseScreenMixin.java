package meow.binary.scavenger.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import meow.binary.scavenger.client.screen.VictoryScreen;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
    @Unique
    private int scavenger$timesPressed = 0;

    @Shadow
    protected abstract Button openScreenButton(Component message, Supplier<Screen> newScreen);

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V"))
    private void addVictoryScreenButton(CallbackInfo ci, @Local GridLayout.RowHelper helper) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean victory = false;
        if (ClientScavengerData.winTimestamp != 0) {
            helper.addChild(Button.builder(Component.translatable("scavenger.open_victory_screen"), (button) -> {
                minecraft.gui.setScreen(new VictoryScreen());
            }).width(98).build(), 1);
            victory = true;
        }


        helper.addChild(Button.builder(Component.translatable("scavenger.restart_run"), (button) -> {
            if (scavenger$timesPressed < 1) {
                button.setMessage(Component.translatable("scavenger.sure").withStyle(ChatFormatting.RED));
                scavenger$timesPressed++;
                return;
            }

            button.active = false;
            ScavengerWorldCreateScreen.queueRestart(minecraft, ClientScavengerData.item, ClientScavengerData.modifier);
            minecraft.getReportingContext().draftReportHandled(minecraft, (Screen) (Object) this, () -> minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE), true);
        }).width(victory ? 98 : 204).build(), victory ? 1 : 2);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void renderInfo(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ClientScavengerData.isEmpty()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        Component modifierName = Modifiers.getName(ClientScavengerData.modifier).withStyle(ChatFormatting.BOLD);
        Component itemName = new ItemStack(ClientScavengerData.item).getHoverName().copy().withStyle(ChatFormatting.BOLD);
        Component activeModifier = Component.translatable("scavenger.active_modifier").append(": ").append(modifierName);
        Component itemToFind = Component.translatable("scavenger.item_to_find").append(": ").append(itemName);
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        int modifierWidth = font.width(activeModifier);
        int itemWidth = font.width(itemToFind);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(width/2f, height - 32);
        if (ClientScavengerData.is(Modifiers.TOURIST)) {
            Component language = Component.translatable("options.language.title").append(": ").append(Component.translatable("language.name").withStyle(ChatFormatting.BOLD));
            int languageWidth = font.width(language);
            guiGraphics.text(font, language, - languageWidth / 2, -2, 0xffffffff, true);
        }
        guiGraphics.text(font, activeModifier, - modifierWidth / 2, 8, 0xffffffff, true);
        guiGraphics.text(font, itemToFind, - itemWidth / 2, 18, 0xffffffff, true);
        guiGraphics.pose().popMatrix();

        float modifierPos = (width - modifierWidth) / 2f;
        float itemPos = (width - itemWidth) / 2f;
        int yPos = height - 32;

        if (mouseX >= modifierPos && mouseX < modifierPos + modifierWidth && mouseY >= yPos + 8 && mouseY < yPos + 17) {
            guiGraphics.tooltip(font, List.of(ClientTooltipComponent.create(Modifiers.getDescription(ClientScavengerData.modifier).getVisualOrderText())),
                    mouseX, mouseY, DefaultTooltipPositioner.INSTANCE,null
            );
        } else if (mouseX >= itemPos && mouseX < itemPos + itemWidth && mouseY >= yPos + 18 && mouseY < yPos + 27) {
            guiGraphics.setTooltipForNextFrame(font, ClientScavengerData.item.getDefaultInstance(), mouseX, mouseY);
        }


    }
}
