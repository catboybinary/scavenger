package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class SoundEngineMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void cancelSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        if (Modifiers.isActive(Modifiers.SILENCE, level)) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }

    @Inject(method = "playDelayed", at = @At("HEAD"), cancellable = true)
    private void cancelSound2(SoundInstance sound, int delay, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        if (Modifiers.isActive(Modifiers.SILENCE, level)) {
            ci.cancel();
        }
    }

    @Inject(method = "queueTickingSound", at = @At("HEAD"), cancellable = true)
    private void cancelSound3(TickableSoundInstance tickableSound, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        if (Modifiers.isActive(Modifiers.SILENCE, level)) {
            ci.cancel();
        }
    }
}
