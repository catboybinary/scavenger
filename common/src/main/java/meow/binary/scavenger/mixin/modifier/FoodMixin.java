package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class FoodMixin {
    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void preventEating(boolean canAlwaysEat, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        if (ClientScavengerData.modifier.equals(Modifiers.VEGETARIAN.getId())) {
            if (!scavenger$hasPlantInHand(player)) {
                cir.setReturnValue(false);
            }
        } else if (ClientScavengerData.modifier.equals(Modifiers.CARNIVORE.getId())) {
            if (!scavenger$hasMeatOrFishInHand(player)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Unique
    private static boolean scavenger$hasMeatOrFishInHand(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).getTags().anyMatch(itemTagKey -> itemTagKey.equals(ItemTags.MEAT) || itemTagKey.equals(ItemTags.FISHES))
                || player.getItemInHand(InteractionHand.OFF_HAND).getTags().anyMatch(itemTagKey -> itemTagKey.equals(ItemTags.MEAT) || itemTagKey.equals(ItemTags.FISHES));
    }

    @Unique
    private static boolean scavenger$hasPlantInHand(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).getTags().anyMatch(itemTagKey -> itemTagKey.equals(Scavenger.VEGETARIAN_FOOD))
                || player.getItemInHand(InteractionHand.OFF_HAND).getTags().anyMatch(itemTagKey -> itemTagKey.equals(Scavenger.VEGETARIAN_FOOD));
    }
}
