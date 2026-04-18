package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(method = "getEffectiveRenderDistance", at = @At("RETURN"), cancellable = true)
    private void modifyEffectiveRenderDistance(CallbackInfoReturnable<Integer> cir) {
        if (ClientScavengerData.is(Modifiers.MOLE)) {
            cir.setReturnValue(Math.min(cir.getReturnValue(), 2));
        }
    }
}
