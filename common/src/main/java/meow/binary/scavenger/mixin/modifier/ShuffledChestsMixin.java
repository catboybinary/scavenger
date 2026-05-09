package meow.binary.scavenger.mixin.modifier;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RandomizableContainer.class)
public interface ShuffledChestsMixin {
    @Shadow
    ResourceKey<LootTable> getLootTable();

    @Shadow
    long getLootTableSeed();

    @Shadow
    void setLootTable(ResourceKey<LootTable> lootTable, long lootTableSeed);

    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    private void scavenger$shuffleLootTable(Player player, CallbackInfo ci) {
        if (player == null || !Modifiers.isActive(Modifiers.SHUFFLED_CHESTS, player.level())) {
            return;
        }

        ResourceKey<LootTable> currentLootTable = this.getLootTable();
        if (currentLootTable == null) {
            return;
        }

        List<ResourceKey<LootTable>> lootTables = BuiltInLootTables.all().stream()
                .filter(lootTable -> lootTable.identifier().getPath().startsWith("chests/"))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        if (lootTables.size() > 1) {
            lootTables.remove(currentLootTable);
        }

        if (lootTables.isEmpty()) {
            return;
        }

        ResourceKey<LootTable> randomLootTable = lootTables.get(player.level().random.nextInt(lootTables.size()));
        this.setLootTable(randomLootTable, this.getLootTableSeed());
    }
}
