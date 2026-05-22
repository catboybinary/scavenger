package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public class ContainerScreenMixin {
    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Inject(method = "renderSlots", at = @At("TAIL"))
    private void renderBarriers(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory inventory && Scavenger.isSlotBlocked(slot.getContainerSlot(), inventory.player.level())) {
                guiGraphics.renderItem(Items.BARRIER.getDefaultInstance(), slot.x, slot.y);
            }
        }
    }

    @Inject(method = "getTooltipFromContainerItem", at = @At("HEAD"), cancellable = true)
    private void disableAdvancedTooltip(ItemStack item, CallbackInfoReturnable<List<Component>> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;

        if (level == null) {
            return;
        }

        if (Modifiers.isActive(Modifiers.TOURIST, level)) {
            cir.setReturnValue(item.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.Default.NORMAL));
            cir.cancel();
        }
    }
}
