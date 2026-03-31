package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.Scavenger;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void inject(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Slot slot = (Slot) (Object) this;

        if (slot.container instanceof Inventory inventory) {
            if (Scavenger.isSlotBlocked(slot.getContainerSlot(), inventory.player.level())) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void deactivate(CallbackInfoReturnable<Boolean> cir) {
        Slot slot = (Slot) (Object) this;

        if (slot.container instanceof Inventory inventory) {
            if (Scavenger.isSlotBlocked(slot.getContainerSlot(), inventory.player.level())) {
                cir.setReturnValue(false);
            }
        }
    }
}
