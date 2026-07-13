package meow.binary.scavenger.mixin;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ScavengerTimeFormat;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(WorldSelectionList.WorldListEntry.class)
public class WorldListEntryMixin {
    private static final String SCAVENGER_DATA_FILE = "scavenger_data.dat";

    @Unique
    private final Map<String, Optional<ScavengerWorldTooltipData>> SCAVENGER_TOOLTIP_CACHE = new HashMap<>();

    @Shadow
    @Final
    private LevelSummary summary;

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void showScavengerTooltip(GuiGraphicsExtractor guiGraphics, int x, int y, boolean hovered, float partialTick, CallbackInfo ci) {
        if (!hovered) {
            return;
        }

        Optional<ScavengerWorldTooltipData> data = scavenger$getTooltipData(summary);
        if (data.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ScavengerWorldTooltipData tooltipData = data.get();
        List<Component> tooltip = new ArrayList<>();

        Component itemName;

        try {
            itemName = new ItemStack(tooltipData.item()).getHoverName().copy().withStyle(ChatFormatting.BOLD);
        } catch (NullPointerException nullPointerException) {
            itemName = Component.literal(tooltipData.item.builtInRegistryHolder().getRegisteredName()).withStyle(ChatFormatting.BOLD);
        }

        tooltip.add(Component.translatable("scavenger.item_to_find")
                .append(": ")
                .append(itemName));
        tooltip.add(Component.translatable("scavenger.active_modifier")
                .append(": ")
                .append(Modifiers.getName(tooltipData.modifierId()).withStyle(ChatFormatting.BOLD)));

        if (tooltipData.completed()) {
            tooltip.add(Component.translatable("scavenger.world_status")
                    .append(": ")
                    .append(Component.translatable("scavenger.world_status.completed").withStyle(ChatFormatting.GREEN)));
            tooltip.add(Component.translatable("scavenger.completion_time")
                    .append(": ")
                    .append(Component.literal(ScavengerTimeFormat.format(tooltipData.winTimestamp(), tooltipData.modifierId())).withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.translatable("scavenger.world_status")
                    .append(": ")
                    .append(Component.translatable("scavenger.world_status.incomplete").withStyle(ChatFormatting.GRAY)));
        }

        int mouseX = (int) minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
        int mouseY = (int) minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());

        guiGraphics.setTooltipForNextFrame(minecraft.font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    @Unique
    private Optional<ScavengerWorldTooltipData> scavenger$getTooltipData(LevelSummary summary) {
        return SCAVENGER_TOOLTIP_CACHE.computeIfAbsent(summary.getLevelId(), this::scavenger$readTooltipData);
    }

    @Unique
    private Optional<ScavengerWorldTooltipData> scavenger$readTooltipData(String levelId) {
        Minecraft minecraft = Minecraft.getInstance();
        LevelStorageSource levelSource = minecraft.getLevelSource();
        Path dataPath = levelSource.getLevelPath(levelId).resolve("data").resolve(Scavenger.MOD_ID).resolve(SCAVENGER_DATA_FILE);

        Optional<ScavengerSavedData> data = ScavengerSavedData.readFromDisk(dataPath);
        if (data.isEmpty() || data.get().isEmpty()) {
            return Optional.empty();
        }

        ScavengerSavedData savedData = data.get();
        Item item = BuiltInRegistries.ITEM.getOptional(savedData.getItemId()).orElse(Items.AIR);

        return Optional.of(new ScavengerWorldTooltipData(item, savedData.getModifierId(), savedData.hasWon(), savedData.getWinTimestamp()));
    }

    private record ScavengerWorldTooltipData(Item item, Identifier modifierId, boolean completed, long winTimestamp) {
    }
}
