package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "canUseSlot", at = @At("HEAD"), cancellable = true)
    private void inject(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Player)) {
            return;
        }

        Level level = entity.level();
        if (Modifiers.isActive(Modifiers.BRITTLE_BONES, level) && slot.getType().equals(EquipmentSlot.Type.HUMANOID_ARMOR)) {
            cir.setReturnValue(false);
            return;
        }

        if (Modifiers.isActive(Modifiers.ONE_ARM, level) && slot.equals(EquipmentSlot.OFFHAND)) {
            cir.setReturnValue(false);
            return;
        }
    }
}
