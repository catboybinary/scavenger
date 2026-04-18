package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.Scavenger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
