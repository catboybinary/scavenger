package meow.binary.scavenger.client.screen;

import meow.binary.scavenger.client.RunHistory;
import meow.binary.scavenger.client.RunRecord;
import meow.binary.scavenger.client.ScavengerTimeFormat;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TierListScreen extends Screen {
    private static final int PANEL_WIDTH = 460;
    private static final int TOP_PADDING = 30;
    private static final int SECTION_HEADER_HEIGHT = 14;
    private static final int ROW_HEIGHT = 18;
    private static final int BOTTOM_PADDING = 32;
    private static final int ITEM_NAME_WIDTH = 100;
    private static final int MODIFIER_WIDTH = 80;
    private static final long SEED_COPIED_DISPLAY_MS = 1200L;

    private final Screen parent;
    private final List<RunRecord> best;
    private final List<RunRecord> worst;

    private int panelX;
    private int panelY;
    private int panelHeight;

    private long seedCopiedAt = -1L;
    private int seedCopiedX;
    private int seedCopiedY;

    public TierListScreen(Screen parent) {
        super(Component.translatable("scavenger.tier_list"));
        this.parent = parent;

        List<RunRecord> sorted = new ArrayList<>(RunHistory.scanAndMerge());
        sorted.sort(Comparator.comparingDouble(RunRecord::totalSeconds));

        int total = sorted.size();
        this.best = new ArrayList<>(sorted.subList(0, Math.min(5, total)));

        int worstStart = Math.min(total, Math.max(5, total - 5));
        List<RunRecord> worstSlice = new ArrayList<>(sorted.subList(worstStart, total));
        Collections.reverse(worstSlice);
        this.worst = worstSlice;
    }

    @Override
    protected void init() {
        int contentHeight;
        if (this.best.isEmpty() && this.worst.isEmpty()) {
            contentHeight = ROW_HEIGHT;
        } else {
            contentHeight = 0;
            if (!this.best.isEmpty()) {
                contentHeight += SECTION_HEADER_HEIGHT + this.best.size() * ROW_HEIGHT;
            }
            if (!this.worst.isEmpty()) {
                contentHeight += SECTION_HEADER_HEIGHT + this.worst.size() * ROW_HEIGHT;
            }
        }

        this.panelHeight = TOP_PADDING + contentHeight + BOTTOM_PADDING;
        this.panelX = this.width / 2 - PANEL_WIDTH / 2;
        this.panelY = this.height / 2 - this.panelHeight / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                .bounds(this.width / 2 - 50, this.panelY + this.panelHeight - 28, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.panelX - 4, this.panelY - 4, this.panelX + PANEL_WIDTH + 4, this.panelY + this.panelHeight + 4, 0xaa000000);
        guiGraphics.fill(this.panelX, this.panelY, this.panelX + PANEL_WIDTH, this.panelY + this.panelHeight, 0xee1a222b);
        guiGraphics.renderOutline(this.panelX, this.panelY, PANEL_WIDTH, this.panelHeight, 0xffffffff);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelY + 12, 0xffffffff);

        if (this.best.isEmpty() && this.worst.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("scavenger.tier_list.empty"), this.width / 2, bestHeaderY(), 0xffbbbbbb);
        } else {
            if (!this.best.isEmpty()) {
                guiGraphics.drawString(this.font, Component.translatable("scavenger.tier_list.best").withStyle(ChatFormatting.BOLD), this.panelX + 12, bestHeaderY(), 0xff9fdce7, false);
            }

            if (!this.worst.isEmpty()) {
                guiGraphics.drawString(this.font, Component.translatable("scavenger.tier_list.worst").withStyle(ChatFormatting.BOLD), this.panelX + 12, worstHeaderY(), 0xff9fdce7, false);
            }

            for (Row row : layoutRows()) {
                renderRow(guiGraphics, row, mouseX, mouseY);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.seedCopiedAt > 0 && System.currentTimeMillis() - this.seedCopiedAt < SEED_COPIED_DISPLAY_MS) {
            guiGraphics.setTooltipForNextFrame(this.font, List.of(Component.translatable("scavenger.victory.seed_copied")), Optional.empty(), this.seedCopiedX, this.seedCopiedY);
        }
    }

    private void renderRow(GuiGraphics guiGraphics, Row row, int mouseX, int mouseY) {
        RunRecord record = row.record();
        int y = row.y();
        int rankX = this.panelX + 12;
        int iconX = this.panelX + 32;
        int itemNameX = this.panelX + 54;
        int modifierX = this.panelX + 160;
        int timeX = this.panelX + 246;
        int seedRight = this.panelX + PANEL_WIDTH - 12;
        int textY = y + 5;

        Item item = BuiltInRegistries.ITEM.getOptional(record.itemId()).orElse(Items.AIR);

        guiGraphics.drawString(this.font, row.rank() + ".", rankX, textY, 0xffbbbbbb, false);
        guiGraphics.renderItem(item.getDefaultInstance(), iconX, y + 1);
        guiGraphics.drawString(this.font, trimToWidth(item.getName().getString(), ITEM_NAME_WIDTH), itemNameX, textY, 0xffffffff, false);
        guiGraphics.drawString(this.font, trimToWidth(Modifiers.getName(record.modifierId()).getString(), MODIFIER_WIDTH), modifierX, textY, 0xffffffff, false);
        guiGraphics.drawString(this.font, ScavengerTimeFormat.format(record.winTimestamp(), record.modifierId()), timeX, textY, 0xffffffff, false);

        String seedText = record.seed().map(String::valueOf).orElse("?");
        int seedTextX = seedRight - this.font.width(seedText);
        boolean hovered = record.seed().isPresent() && isWithinSeedCell(mouseX, mouseY, seedTextX, seedRight, y);
        guiGraphics.drawString(this.font, seedText, seedTextX, textY, hovered ? 0xffffffff : 0xffbbbbbb, false);
    }

    private String trimToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        return this.font.plainSubstrByWidth(text, Math.max(0, maxWidth - this.font.width(ellipsis))) + ellipsis;
    }

    private boolean isWithinSeedCell(double mouseX, double mouseY, int left, int right, int y) {
        return mouseX >= left && mouseX < right && mouseY >= y && mouseY < y + ROW_HEIGHT;
    }

    private int bestHeaderY() {
        return this.panelY + TOP_PADDING;
    }

    private int worstHeaderY() {
        int y = this.panelY + TOP_PADDING;
        if (!this.best.isEmpty()) {
            y += SECTION_HEADER_HEIGHT + this.best.size() * ROW_HEIGHT;
        }

        return y;
    }

    private List<Row> layoutRows() {
        List<Row> rows = new ArrayList<>();

        if (!this.best.isEmpty()) {
            int y = bestHeaderY() + SECTION_HEADER_HEIGHT;
            for (int i = 0; i < this.best.size(); i++) {
                rows.add(new Row(this.best.get(i), i + 1, y));
                y += ROW_HEIGHT;
            }
        }

        if (!this.worst.isEmpty()) {
            int y = worstHeaderY() + SECTION_HEADER_HEIGHT;
            for (int i = 0; i < this.worst.size(); i++) {
                rows.add(new Row(this.worst.get(i), i + 1, y));
                y += ROW_HEIGHT;
            }
        }

        return rows;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0 && trySeedClick(event.x(), event.y())) {
            return true;
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    private boolean trySeedClick(double mouseX, double mouseY) {
        int seedRight = this.panelX + PANEL_WIDTH - 12;

        for (Row row : layoutRows()) {
            Optional<Long> seed = row.record().seed();
            if (seed.isEmpty()) {
                continue;
            }

            String seedText = String.valueOf(seed.get());
            int seedTextX = seedRight - this.font.width(seedText);
            if (!isWithinSeedCell(mouseX, mouseY, seedTextX, seedRight, row.y())) {
                continue;
            }

            this.minecraft.keyboardHandler.setClipboard(seedText);
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.25f));
            this.seedCopiedAt = System.currentTimeMillis();
            this.seedCopiedX = (int) mouseX;
            this.seedCopiedY = (int) mouseY;
            return true;
        }

        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private record Row(RunRecord record, int rank, int y) {
    }
}
