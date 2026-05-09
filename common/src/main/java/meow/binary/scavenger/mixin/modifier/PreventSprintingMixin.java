package meow.binary.scavenger.mixin.modifier;

import com.mojang.datafixers.util.Either;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PreventSprintingMixin {
    @Inject(method = "isMobilityRestricted", at = @At("HEAD"), cancellable = true)
    private void scavenger$restrictMobility(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (Modifiers.isActive(Modifiers.SNAIL, player.level())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
    private void cancelSleep(BlockPos bedPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        Player player = (Player) (Object) this;
        if (Modifiers.isActive(Modifiers.INSOMNIA, player.level())) {
            cir.setReturnValue(Either.left(Scavenger.INSOMNIA_PROBLEM));
        }
    }
}
