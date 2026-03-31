package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.Scavenger;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class OnlyHotbarMixin {
    @Shadow
    @Final
    public Player player;

    @Inject(method = "getFreeSlot", at = @At("RETURN"), cancellable = true)
    private void inject(CallbackInfoReturnable<Integer> cir) {
        Level level = player.level();

        if (Scavenger.isSlotBlocked(cir.getReturnValue(), level)) {
            cir.setReturnValue(-1);
        }
    }
}
