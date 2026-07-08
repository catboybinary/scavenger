package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
public class FoodMixin {
    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private void preventEating(LivingEntity entity, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Player) || stack.get(DataComponents.FOOD) == null) {
            return;
        }

        if (ClientScavengerData.is(Modifiers.VEGETARIAN)) {
            if (!stack.is(Scavenger.VEGETARIAN_FOOD)) {
                cir.setReturnValue(false);
            }
        } else if (ClientScavengerData.is(Modifiers.CARNIVORE)) {
            if (!stack.is(ItemTags.MEAT) && !stack.is(ItemTags.FISHES)) {
                cir.setReturnValue(false);
            }
        }
    }
}
