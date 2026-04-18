package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
    private Object modifySensitivity(OptionInstance<?> option) {
        if (ClientScavengerData.is(Modifiers.TURTLE)) {
            return 0d;
        }

        if (ClientScavengerData.is(Modifiers.SONIC)) {
            return 1d;
        }

        return option.get();
    }

    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1))
    private Object modifyInvertMouseX(OptionInstance<?> option) {
        boolean inverted = (Boolean) option.get();
        return ClientScavengerData.is(Modifiers.DRUNK) != inverted;
    }

    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 2))
    private Object modifyInvertMouseY(OptionInstance<?> option) {
        boolean inverted = (Boolean) option.get();
        return ClientScavengerData.is(Modifiers.DRUNK) != inverted;
    }
}
