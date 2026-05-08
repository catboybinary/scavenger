package meow.binary.scavenger.mixin;

import meow.binary.scavenger.client.ScavengerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "disconnectFromWorld", at = @At("HEAD"))
    private void scavenger$resetClientModifierState(Component reason, CallbackInfo ci) {
        ScavengerClient.onClientDisconnect();
    }
}
