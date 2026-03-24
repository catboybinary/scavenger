package meow.binary.scavenger.mixin.modifier;

import it.hurts.shatterbyte.shatterlib.ShatterLib;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class VillagerLevelMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void cancelSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (!Modifiers.isActive(Modifiers.ASOCIAL, level)) {
            return;
        }

        if (entity instanceof Villager) {
            ShatterLib.LOGGER.warn("Asocial villager removed!");
            cir.setReturnValue(false);
        }
    }
}
