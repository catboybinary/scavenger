package meow.binary.scavenger.mixin;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(WorldSelectionList.WorldListEntry.class)
public class WorldListEntryMixin {
    private static final String SCAVENGER_DATA_FILE = "scavenger_data.dat";
    private static final Map<String, Optional<ScavengerWorldTooltipData>> SCAVENGER_TOOLTIP_CACHE = new HashMap<>();

    @Shadow
    @Final
    private LevelSummary summary;

    @Inject(method = "renderContent", at = @At("TAIL"))
    private void showScavengerTooltip(GuiGraphics guiGraphics, int x, int y, boolean hovered, float partialTick, CallbackInfo ci) {
        if (!hovered) {
            return;
        }

        Optional<ScavengerWorldTooltipData> data = getTooltipData(summary);
        if (data.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ScavengerWorldTooltipData tooltipData = data.get();
        List<Component> tooltip = new ArrayList<>(List.of(
                Component.translatable("scavenger.item_to_find")
                        .append(": ")
                        .append(tooltipData.item().getName().copy().withStyle(ChatFormatting.BOLD)),
                Component.translatable("scavenger.active_modifier")
                        .append(": ")
                        .append(Modifiers.getName(tooltipData.modifierId()).withStyle(ChatFormatting.BOLD))
        ));

        if (tooltipData.completed()) {
            tooltip.add(Component.translatable("scavenger.world_status")
                    .append(": ")
                    .append(Component.translatable("scavenger.world_status.completed").withStyle(ChatFormatting.GREEN)));
            tooltip.add(Component.translatable("scavenger.completion_time")
                    .append(": ")
                    .append(Component.literal(formatTime(tooltipData.winTimestamp(), tooltipData.modifierId())).withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.translatable("scavenger.world_status")
                    .append(": ")
                    .append(Component.translatable("scavenger.world_status.incomplete").withStyle(ChatFormatting.GRAY)));
        }

        int mouseX = (int) minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
        int mouseY = (int) minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());

        guiGraphics.setTooltipForNextFrame(minecraft.font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    private static Optional<ScavengerWorldTooltipData> getTooltipData(LevelSummary summary) {
        return SCAVENGER_TOOLTIP_CACHE.computeIfAbsent(summary.getLevelId(), WorldListEntryMixin::readTooltipData);
    }

    private static Optional<ScavengerWorldTooltipData> readTooltipData(String levelId) {
        Minecraft minecraft = Minecraft.getInstance();
        LevelStorageSource levelSource = minecraft.getLevelSource();
        Path dataPath = levelSource.getLevelPath(levelId).resolve("data").resolve(SCAVENGER_DATA_FILE);

        if (!Files.isRegularFile(dataPath)) {
            return Optional.empty();
        }

        try {
            CompoundTag root = NbtIo.readCompressed(dataPath, NbtAccounter.uncompressedQuota());
            CompoundTag data = root.getCompound("data").orElse(root);
            Optional<Identifier> itemId = data.getString("itemId").map(Identifier::tryParse);
            Optional<Identifier> modifierId = data.getString("modifier").map(Identifier::tryParse);
            boolean completed = data.getBooleanOr("hasWon", false);
            long winTimestamp = data.getLongOr("winTimestamp", 0L);

            if (itemId.isEmpty() || modifierId.isEmpty()) {
                return Optional.empty();
            }

            Item item = BuiltInRegistries.ITEM.getOptional(itemId.get()).orElse(Items.AIR);
            if (item.equals(Items.AIR) && modifierId.get().equals(Modifiers.NONE.getId())) {
                return Optional.empty();
            }

            return Optional.of(new ScavengerWorldTooltipData(item, modifierId.get(), completed, winTimestamp));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private static String formatTime(long ticks, Identifier modifierId) {
        double tickrate = modifierId.equals(Modifiers.SPEED_UP.getId()) ? 40d : 20d;
        double totalSeconds = ticks / tickrate;
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        int millis = (int) ((totalSeconds - Math.floor(totalSeconds)) * 100);

        return String.format("%d:%02d:%02d.%02d", hours, minutes, seconds, millis);
    }

    private record ScavengerWorldTooltipData(Item item, Identifier modifierId, boolean completed, long winTimestamp) {
    }
}
