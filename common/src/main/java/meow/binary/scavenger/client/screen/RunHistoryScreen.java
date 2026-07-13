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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RunHistoryScreen extends Screen {
    private static final int PANEL_WIDTH = 246;
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

    public RunHistoryScreen(Screen parent) {
        super(Component.translatable("scavenger.run_history"));
        this.parent = parent;

        List<RunRecord> sorted = new ArrayList<>(RunHistory.scanAndMerge());
        sorted.sort(Comparator.comparingDouble(RunRecord::totalSeconds));

        int total = sorted.size();
        this.best = new ArrayList<>(sorted.subList(0, Math.min(5, total)));

        int worstStart = Math.clamp(total - 5, 5, total);
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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.centeredText(this.font, this.title, this.width / 2, this.panelY + 12, 0xffffffff);

        if (this.best.isEmpty() && this.worst.isEmpty()) {
            guiGraphics.centeredText(this.font, Component.translatable("scavenger.run_history.empty"), this.width / 2, this.panelY + TOP_PADDING, 0xffbbbbbb);
        } else {

        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return super.mouseClicked(event, isDoubleClick);
    }


    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
