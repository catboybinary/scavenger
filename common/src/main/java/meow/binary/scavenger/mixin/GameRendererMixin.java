package meow.binary.scavenger.mixin;

import meow.binary.scavenger.client.ScavengerClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "checkEntityPostEffect", at = @At("RETURN"))
    private void reapplyNoirPostEffect(Entity entity, CallbackInfo ci) {
        ScavengerClient.enforceNoirPostEffect();
    }
}
