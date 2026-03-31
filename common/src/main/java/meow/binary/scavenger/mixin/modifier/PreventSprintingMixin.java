package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PreventSprintingMixin {
    @Inject(method = "canSprint", at = @At("HEAD"), cancellable = true)
    private void cancelSprint(CallbackInfoReturnable<Boolean> cir) {
        if (ClientScavengerData.is(Modifiers.SNAIL)) {
            cir.setReturnValue(false);
        }
    }
}
