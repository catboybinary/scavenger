package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin {
    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    private void preventCycling(CallbackInfoReturnable<CameraType> cir) {
        CameraType type = (CameraType) (Object) this;
        if (ClientScavengerData.is(Modifiers.NPC) || ClientScavengerData.is(Modifiers.MAIN_CHARACTER)) {
            cir.setReturnValue(type);
        }
    }
}
