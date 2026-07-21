package meow.binary.scavenger.client.screen;

import meow.binary.scavenger.client.RunHistory;
import meow.binary.scavenger.client.RunRecord;
import meow.binary.scavenger.client.ScavengerTimeFormat;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DetailsScreen extends Screen {
    private static final int ROW_HEIGHT = 12;
    private static final int TOP_PADDING = 28;
    private static final int BOTTOM_PADDING = 36;
    private static final int PADDING = 8;
    private static final int SCROLLBAR_WIDTH = 4;

    private final Screen prevScreen;
    private final List<RunRecord> records;

    private int panelX, panelY, panelWidth, panelHeight;
    private int listX, listY, listWidth, listHeight;
    private int scrollOffset;
    private int maxScroll;
    private boolean scrolling;
    private boolean ascending = true;

    public DetailsScreen(Screen prevScreen) {
        super(Component.translatable("scavenger.run_history.more"));
        this.prevScreen = prevScreen;

        List<RunRecord> sorted = new ArrayList<>(RunHistory.scanAndMerge());
        sorted.sort(Comparator.comparingDouble(RunRecord::totalSeconds));
        this.records = sorted;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(420, this.width - 40);
        int contentHeight = records.size() * ROW_HEIGHT;
        int maxVisibleHeight = this.height - TOP_PADDING - BOTTOM_PADDING - 40;
        panelHeight = TOP_PADDING + Math.min(contentHeight, maxVisibleHeight) + BOTTOM_PADDING;

        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        listX = panelX + PADDING;
        listY = panelY + TOP_PADDING;
        listWidth = panelWidth - PADDING * 2 - SCROLLBAR_WIDTH - 4;
        listHeight = panelHeight - TOP_PADDING - BOTTOM_PADDING;

        maxScroll = Math.max(0, contentHeight - listHeight);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                .bounds(this.width / 2 - 82, panelY + panelHeight - 26, 80, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("scavenger.run_history.sort", ascending?"↑":"↓"), this::toggleSort)
                .bounds(this.width / 2 + 2, panelY + panelHeight - 26, 80, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, panelY + 10, 0xffffffff);

        if (records.isEmpty()) {
            guiGraphics.centeredText(this.font, Component.translatable("scavenger.run_history.empty"),
                    this.width / 2, panelY + panelHeight / 2, 0xffbbbbbb);
        } else {
            guiGraphics.enableScissor(listX, listY, listX + listWidth + 1, listY + listHeight);

            int firstVisible = Math.max(0, scrollOffset / ROW_HEIGHT - 1);
            int lastVisible = Math.min(records.size(), (scrollOffset + listHeight) / ROW_HEIGHT + 1);

            String maxIndex = records.size()+". ";
            int indexWidth = font.width(maxIndex);

            for (int i = firstVisible; i < lastVisible; i++) {
                RunRecord record = records.get(i);
                int rowY = listY + i * ROW_HEIGHT - scrollOffset;

                ItemStack stack = null;
                String itemName;
                try {
                    stack = new ItemStack(BuiltInRegistries.ITEM.getValue(record.itemId()));
                    itemName = stack.getStyledHoverName().getString();
                } catch (Throwable _) {
                    itemName = record.itemId().toString();
                }
                String modifierName = Modifiers.getName(record.modifierId()).getString();
                String time = ScavengerTimeFormat.format(record.winTimestamp(), record.modifierId(), true);

                String index = (i + 1) + ".";

                if (i % 2 != 0) {
                    guiGraphics.fill(listX, rowY - 1, listX + listWidth + 1, rowY + ROW_HEIGHT - 1, 0x22aaaaaa);
                }

                int timeWidth = this.font.width(time);
                int modifierWidth = this.font.width(modifierName);
                int gap = 6;

                guiGraphics.text(this.font, index, listX + 1, rowY + 1, 0xffaaaaaa, false);

                int itemX = listX + indexWidth;
                int nameX = itemX + 12;
                int modifierX = listX + listWidth - timeWidth - gap - modifierWidth;
                int maxNameWidth = modifierX - nameX - gap;

                if (this.font.width(itemName) > maxNameWidth) {
                    itemName = this.font.plainSubstrByWidth(itemName, maxNameWidth - this.font.width("...")) + "...";
                }

                guiGraphics.text(this.font, itemName, nameX, rowY + 1, 0xffcccccc, false);
                if (stack != null) {
                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().translate(itemX + 1, rowY + 1);
                    guiGraphics.pose().scale(0.5f);
                    guiGraphics.item(stack, 0, 0);
                    guiGraphics.pose().popMatrix();
                }

                guiGraphics.text(this.font, modifierName, modifierX, rowY + 1, 0xff88aacc, false);
                guiGraphics.text(this.font, time, listX + listWidth - timeWidth, rowY + 1, 0xffeedd55, false);
            }

            guiGraphics.disableScissor();

            int hoveredIndex = (mouseY - listY + scrollOffset) / ROW_HEIGHT;
            if (mouseX >= listX && mouseX <= listX + listWidth
                    && mouseY >= listY && mouseY <= listY + listHeight
                    && hoveredIndex >= 0 && hoveredIndex < records.size()) {
                RunRecord hovered = records.get(hoveredIndex);
                ItemStack tooltipStack = null;
                try {
                    tooltipStack = new ItemStack(BuiltInRegistries.ITEM.getValue(hovered.itemId()));
                } catch (Throwable _) {}
                List<FormattedCharSequence> tooltip = new ArrayList<>();
                if (tooltipStack != null) {
                    for (Component component : Screen.getTooltipFromItem(this.minecraft, tooltipStack)) {
                        tooltip.add(component.getVisualOrderText());
                    }
                    tooltip.add(FormattedCharSequence.EMPTY);
                }
                tooltip.add(Modifiers.getName(hovered.modifierId()).withStyle(ChatFormatting.GRAY).getVisualOrderText());
                tooltip.add(Component.translatable("commands.seed.success", hovered.seed().orElse(0L).toString()).withStyle(ChatFormatting.GREEN).getVisualOrderText());
                guiGraphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
            }

            if (maxScroll > 0) {
                int scrollbarX = listX + listWidth + 2;
                int barHeight = Math.max(16, listHeight * listHeight / (listHeight + maxScroll));
                int barY = listY + scrollOffset * (listHeight - barHeight) / maxScroll;
                guiGraphics.fill(scrollbarX, listY, scrollbarX + SCROLLBAR_WIDTH, listY + listHeight, 0x33ffffff);
                guiGraphics.fill(scrollbarX, barY, scrollbarX + SCROLLBAR_WIDTH, barY + barHeight, 0xccffffff);
            }
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0 && mouseX >= listX && mouseX <= listX + listWidth + SCROLLBAR_WIDTH
                && mouseY >= listY && mouseY <= listY + listHeight) {
            scrollOffset = (int) Math.clamp(scrollOffset - scrollY * ROW_HEIGHT * 3, 0, maxScroll);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && maxScroll > 0) {
            int scrollbarX = listX + listWidth + 2;
            if (event.x() >= scrollbarX && event.x() <= scrollbarX + SCROLLBAR_WIDTH
                    && event.y() >= listY && event.y() <= listY + listHeight) {
                scrolling = true;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (scrolling) {
            int barHeight = Math.max(16, listHeight * listHeight / (listHeight + maxScroll));
            float ratio = (float) (event.y() - listY - barHeight / 2f) / (listHeight - barHeight);
            scrollOffset = (int) Math.clamp(ratio * maxScroll, 0, maxScroll);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && scrolling) {
            scrolling = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(prevScreen);
    }

    private void toggleSort(Button button) {
        ascending = !ascending;
        records.sort(ascending
                ? Comparator.comparingDouble(RunRecord::totalSeconds)
                : Comparator.comparingDouble(RunRecord::totalSeconds).reversed());
        scrollOffset = 0;

        button.setMessage(Component.translatable("scavenger.run_history.sort", ascending?"↑":"↓"));
    }
}
