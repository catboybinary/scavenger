package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "addRenderableWidget", at = @At("HEAD"), cancellable = true)
    public <T extends GuiEventListener & Renderable & NarratableEntry> void onWidgetAdded(T widget, CallbackInfoReturnable<T> cir) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        if (!Modifiers.isActive(Modifiers.UNEDUCATED, level)) {
            return;
        }

        if (widget instanceof ImageButton image && image.sprites != null && image.sprites.equals(RecipeBookComponent.RECIPE_BUTTON_SPRITES)) {
            cir.setReturnValue(null);
        }
    }
}
