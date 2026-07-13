package meow.binary.scavenger.client.screen.widget;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import meow.binary.scavenger.client.RunRecord;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class RunRecordWidget extends AbstractWidget {
    Item item;
    Identifier modifierId;
    String worldName;
    long timestamp;

    Tween tween = Tween.create();
    double delay;

    public RunRecordWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void startAnimation() {

    }

    public static RunRecordWidget from(RunRecord runRecord) {
        RunRecordWidget widget = new RunRecordWidget(0, 0, 96, 32);
        widget.item = BuiltInRegistries.ITEM.getValue(runRecord.itemId());
        widget.modifierId = runRecord.modifierId();
        widget.timestamp = runRecord.winTimestamp();
        widget.worldName = runRecord.levelId();

        return widget;
    }

    public RunRecordWidget withDelay(double seconds) {
        this.delay = seconds;
        return this;
    }
}
