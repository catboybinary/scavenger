package meow.binary.scavenger.mixin;

import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
   @Inject(method = "init", at = @At("HEAD"), cancellable = true)
   private void openPendingRestart(CallbackInfo ci) {
       if (!ScavengerWorldCreateScreen.hasPendingRestart()) {
           return;
       }

       Minecraft minecraft = Minecraft.getInstance();
       CreateWorldScreen createWorldScreen = (CreateWorldScreen) (Object) this;
       createWorldScreen.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.HARDCORE);
       ScavengerWorldCreateScreen.PendingRestart restart = ScavengerWorldCreateScreen.consumePendingRestart();
       minecraft.setScreen(new ScavengerWorldCreateScreen(createWorldScreen, minecraft, restart.item(), restart.modifier(), restart.worldName(), true));
       ci.cancel();
   }

   @Inject(method = "onCreate", at = @At("HEAD"), cancellable = true)
   private void openScavengerWorldCreateScreen(CallbackInfo ci) {
       if (ScavengerWorldCreateScreen.isCreatingVanillaWorld()) {
           return;
       }

       Minecraft minecraft = Minecraft.getInstance();
       CreateWorldScreen createWorldScreen = (CreateWorldScreen) (Object) this;
       minecraft.setScreen(new ScavengerWorldCreateScreen(createWorldScreen, minecraft));
       ci.cancel();
   }
}
