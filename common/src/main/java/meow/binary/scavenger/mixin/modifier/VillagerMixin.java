package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void scavenger$remove(CallbackInfo ci) {
        Villager self = (Villager)(Object)this;
        if (self.level() == null || self.level().isClientSide()) {
            return;
        }

        if (!Modifiers.isActive(Modifiers.ASOCIAL, self.level())) return;

        self.discard(); // or kill()
    }
}