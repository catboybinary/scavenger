package meow.binary.scavenger.client.screen;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.RunHistory;
import meow.binary.scavenger.client.RunRecord;
import meow.binary.scavenger.client.screen.widget.RunRecordWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunHistoryScreen extends Screen {
    private static final int PANEL_WIDTH = 228;
    private static final int COLUMN_WIDTH = 96;
    private static final int TOP_PADDING = 28;
    private static final int HEADER_HEIGHT = 12;
    private static final int ROW_HEIGHT = 32;
    private static final int ROW_GAP = 4;
    private static final int BOTTOM_PADDING = 36;
    private static final int COLUMN_GAP = 24;

    private final Screen parent;
    private final List<RunRecordWidget> bestWidgets = new ArrayList<>();
    private final List<RunRecordWidget> worstWidgets = new ArrayList<>();
    private final int itemsFound;
    private int itemsTotal = 0;

    private int panelX;
    private int panelY;
    private int panelHeight;
    private boolean empty;

    public RunHistoryScreen(Screen parent) {
        super(Component.translatable("scavenger.run_history"));
        this.parent = parent;

        List<RunRecord> sorted = new ArrayList<>(RunHistory.scanAndMerge());
        sorted.sort(Comparator.comparingDouble(RunRecord::totalSeconds));

        Set<Identifier> uniqueItems = new HashSet<>();
        for (RunRecord record : sorted) {
            uniqueItems.add(record.itemId());
        }
        this.itemsFound = uniqueItems.size();

        if (Minecraft.getInstance().level != null) {
            try {
                this.itemsTotal = countRollableItems(uniqueItems);
            } catch (Exception _) {
            }
        }

        int total = sorted.size();
        if (total == 0) {
            this.empty = true;
            return;
        }

        List<RunRecord> best = new ArrayList<>(sorted.subList(0, Math.min(5, total)));
        List<RunRecord> worstSlice;
        if (total <= 5) {
            worstSlice = List.of();
        } else {
            int worstStart = Math.max(total - 5, 0);
            worstSlice = new ArrayList<>(sorted.subList(worstStart, total));
            Collections.reverse(worstSlice);
        }

        for (int i = 0; i < best.size(); i++) {
            RunRecordWidget widget = RunRecordWidget.from(best.get(i), 0, 0, COLUMN_WIDTH, ROW_HEIGHT, false, i);
            widget.startAnimation(i);
            bestWidgets.add(widget);
        }

        for (int i = 0; i < worstSlice.size(); i++) {
            RunRecordWidget widget = RunRecordWidget.from(worstSlice.get(i), 0, 0, COLUMN_WIDTH, ROW_HEIGHT, true, i);
            widget.startAnimation(i);
            worstWidgets.add(widget);
        }
    }

    @Override
    protected void init() {
        int maxRows = Math.max(bestWidgets.size(), worstWidgets.size());
        int rowStep = ROW_HEIGHT + ROW_GAP;
        int contentHeight = empty ? ROW_HEIGHT : HEADER_HEIGHT + maxRows * rowStep - ROW_GAP;

        panelHeight = TOP_PADDING + contentHeight + BOTTOM_PADDING;
        panelX = this.width / 2 - PANEL_WIDTH / 2;
        panelY = this.height / 2 - panelHeight / 2;

        int bestColX = panelX + 6;
        int worstColX = panelX + 6 + COLUMN_WIDTH + COLUMN_GAP;
        int rowsY = panelY + TOP_PADDING + HEADER_HEIGHT;

        for (int i = 0; i < bestWidgets.size(); i++) {
            RunRecordWidget widget = bestWidgets.get(i);
            widget.setPosition(bestColX, rowsY + i * rowStep);
            this.addRenderableWidget(widget);
        }

        for (int i = 0; i < worstWidgets.size(); i++) {
            RunRecordWidget widget = worstWidgets.get(i);
            widget.setPosition(worstColX, rowsY + i * rowStep);
            this.addRenderableWidget(widget);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                .bounds(this.width / 2 - 84, panelY + panelHeight - 18, 82, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("scavenger.run_history.more"), button -> Minecraft.getInstance().setScreenAndShow(new DetailsScreen(this)))
                .bounds(this.width / 2 + 2, panelY + panelHeight - 18, 82, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x55fdcd23, 0x11000000);
        //guiGraphics.fill(0, 0, this.width, this.height, 0x77000000);
        //guiGraphics.fill(panelX - 4, panelY - 4, panelX + PANEL_WIDTH + 4, panelY + panelHeight + 4, 0xaa000000);
        //guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, 0xee1a222b);
        //guiGraphics.outline(panelX, panelY, PANEL_WIDTH, panelHeight, 0xffffffff);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, panelY + 10, 0xffffffff);

        if (itemsTotal > 0) {
            guiGraphics.centeredText(this.font, Component.translatable("scavenger.items_found", itemsFound, itemsTotal), this.width / 2, panelY + panelHeight - 28, 0xffffffff);
        }

        if (empty) {
            guiGraphics.centeredText(this.font, Component.translatable("scavenger.run_history.empty"),
                    this.width / 2, panelY + TOP_PADDING, 0xffbbbbbb);
        } else {
            int bestColX = panelX + 6;
            int worstColX = panelX + 6 + COLUMN_WIDTH + COLUMN_GAP;
            int headerY = panelY + TOP_PADDING;

            Component bestHeader = Component.translatable("scavenger.run_history.best");
            guiGraphics.text(this.font, bestHeader,
                    bestColX + COLUMN_WIDTH / 2 - this.font.width(bestHeader) / 2,
                    headerY, 0xffffcc00, false);

            Component worstHeader = Component.translatable("scavenger.run_history.worst");
            guiGraphics.text(this.font, worstHeader,
                    worstColX + COLUMN_WIDTH / 2 - this.font.width(worstHeader) / 2,
                    headerY, 0xffffcc00, false);
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private static int countRollableItems(Set<Identifier> foundItems) {
        Set<Item> configuredItems;
        if (Scavenger.CONFIG.gameplay.rollableItems == null) {
            configuredItems = Set.of();
        } else {
            configuredItems = new HashSet<>(Scavenger.CONFIG.gameplay.rollableItems);
            configuredItems.removeIf(i -> i == null || i == Items.AIR);
        }

        return (int) BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .filter(item -> !item.getDefaultInstance().is(Scavenger.UNROLLABLE_BY_DEFAULT))
                .filter(item -> {
                    if (configuredItems.isEmpty()) return true;
                    boolean isConfigured = configuredItems.contains(item);
                    boolean poolAllows = Scavenger.CONFIG.gameplay.rollableItemsIsBlacklist != isConfigured;
                    return poolAllows || foundItems.contains(BuiltInRegistries.ITEM.getKey(item));
                })
                .count();
    }
}
