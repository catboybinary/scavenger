package meow.binary.scavenger.mixin.modifier;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import meow.binary.scavenger.Scavenger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class ContainerScreenMixin {
    @Inject(method = "renderSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;isActive()Z", shift = At.Shift.BEFORE), cancellable = true)
    private void renderBarrier(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci, @Local(name = "slot") Slot slot) {
        if (slot.container instanceof Inventory inventory && Scavenger.isSlotBlocked(slot.getContainerSlot(), inventory.player.level())) {
            guiGraphics.renderItem(Items.BARRIER.getDefaultInstance(), slot.x, slot.y);
        }
    }
}
