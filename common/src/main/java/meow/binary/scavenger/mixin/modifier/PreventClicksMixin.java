package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.Scavenger;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class PreventClicksMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void scavenger$holeyPockets(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
//        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
//
//        if (slotId < 0 || slotId >= menu.slots.size()) return;
//        Slot slot = menu.slots.get(slotId);
//
//        if (slot.container instanceof Inventory) {
//            if (Scavenger.isSlotBlocked(slot.getContainerSlot(), player.level())) {
//                ci.cancel();
//            }
//        }
    }
}
