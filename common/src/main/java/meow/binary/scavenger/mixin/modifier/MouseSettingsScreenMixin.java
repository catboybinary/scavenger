package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.mixin.OptionsSubScreenAccessor;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseSettingsScreen.class)
public class MouseSettingsScreenMixin {
    @Inject(method = "addOptions", at = @At("TAIL"))
    private void disableInvertOptions(CallbackInfo ci) {
        if (ClientScavengerData.is(Modifiers.DRUNK)) {
            OptionsSubScreenAccessor accessor = (OptionsSubScreenAccessor) this;
            AbstractWidget invertX = accessor.getList().findOption(accessor.getOptions().invertMouseX());
            AbstractWidget invertY = accessor.getList().findOption(accessor.getOptions().invertMouseY());
            if (invertX != null) invertX.active = false;
            if (invertY != null) invertY.active = false;
        }
    }
}
