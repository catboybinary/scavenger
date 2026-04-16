package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Button.class)
public class RemoveRecipeBookMixin {
    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    public void onPress(CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        if (!Modifiers.isActive(Modifiers.UNEDUCATED, level)) {
            return;
        }

        Button button = (Button) (Object) this;

        if (button instanceof ImageButton image && image.sprites != null && image.sprites.equals(RecipeBookComponent.RECIPE_BUTTON_SPRITES)) {
            ci.cancel();
        }
    }
}
