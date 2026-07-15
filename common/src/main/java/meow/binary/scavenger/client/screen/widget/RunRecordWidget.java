package meow.binary.scavenger.client.screen.widget;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.client.RunRecord;
import meow.binary.scavenger.client.ScavengerTimeFormat;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class RunRecordWidget extends AbstractWidget {
    private static final float SMALL_SCALE = 0.75f;
    private static final int ITEM_SIZE = 16;

    private static final ShatterColor GOLD = new ShatterColor(0xfff4e010);
    private static final ShatterColor SILVER = new ShatterColor(0xffbababa);
    private static final ShatterColor BRONZE = new ShatterColor(0xfff1842f);

    private RunRecord record;
    private boolean rightColumn;
    private int placeIndex;
    private float revealProgress;

    private Tween revealTween = Tween.create();

    public RunRecordWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public static RunRecordWidget from(RunRecord record, int x, int y, int width, int height, boolean rightColumn, int placeIndex) {
        RunRecordWidget widget = new RunRecordWidget(x, y, width, height);
        widget.record = record;
        widget.rightColumn = rightColumn;
        widget.placeIndex = placeIndex;
        return widget;
    }

    public void startAnimation(int index) {
        revealTween.tweenMethod(this::setRevealProgress, 0f, 1f, 0.3)
                .setEaseType(EaseType.EASE_OUT)
                .setTransitionType(TransitionType.CUBIC)
                .setDelay(index * 0.075);
        revealTween.start();
    }

    public void setRevealProgress(float progress) {
        this.revealProgress = progress;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (record == null) return;

        guiGraphics.fill(this.getX(), this.getY(), this.getX()+this.getWidth(), this.getY()+this.getHeight(), 0x55ff0000);

        Font font = Minecraft.getInstance().font;
        float alpha = revealProgress;
        String time = ScavengerTimeFormat.format(record.winTimestamp(), record.modifierId(), false);
        String worldName = record.levelId();
        String modifierName = Modifiers.getName(record.modifierId()).getString();
        ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.getValue(record.itemId()));

        ShatterColor timeColor;
        switch (placeIndex) {
            case 0 -> timeColor = GOLD;
            case 1 -> timeColor = SILVER;
            case 2 -> timeColor = BRONZE;
            default -> timeColor = null;
        }
        int textColor = timeColor != null
                ? withAlpha(timeColor.getARGB(), (int) (0xff * alpha))
                : withAlpha(0xffffffff, (int) (0xff * alpha));
        int dimColor = withAlpha(0xffbbbbbb, (int) (0xff * alpha));

        float timeScale = 1.5f;
        float itemScale = 1.5f;
        float timeWidth = font.width(time) * timeScale;

        float itemX, itemY, timeX, textAlignX;
        itemY = ITEM_SIZE;
        if (rightColumn) {
            itemX = ITEM_SIZE;
            timeX = this.width - timeWidth - 2;
            textAlignX = timeX + timeWidth;
        } else {
            itemX = this.width - ITEM_SIZE;
            timeX = 2;
            textAlignX = timeX;
        }

        float xOffset = (1f - alpha) * 16f * (rightColumn ? -1f : 1f);

        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.getX() + xOffset, this.getY());

        if (revealProgress > 0) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(itemX, itemY);
            guiGraphics.pose().scale(itemScale, itemScale);
            guiGraphics.item(itemStack, -8, -8);
            guiGraphics.pose().popMatrix();
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(timeX, 2);
        guiGraphics.pose().scale(timeScale, timeScale);
        guiGraphics.text(font, time, 0, 0, textColor, false);
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().pushMatrix();
        float nameX = textAlignX;
        guiGraphics.pose().translate(nameX, 16);
        guiGraphics.pose().scale(SMALL_SCALE, SMALL_SCALE);

        if (rightColumn) {
            guiGraphics.text(font, worldName, -font.width(worldName), 0, dimColor, false);
            guiGraphics.text(font, modifierName, -font.width(modifierName), 10, dimColor, false);
        } else {
            guiGraphics.text(font, worldName, 0, 0, dimColor, false);
            guiGraphics.text(font, modifierName, 0, 10, dimColor, false);
        }
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().popMatrix();
//        if (record.seed().isPresent()) {
//            int seedX = rightColumn ? itemX + ITEM_SIZE * 2 + 2 : itemX - 8;
//            seedHovered = mouseX >= seedX && mouseX <= seedX + 8
//                    && mouseY >= this.getY() + this.height - 8
//                    && mouseY <= this.getY() + this.height;
//            int seedColor = seedHovered
//                    ? withAlpha(0xffaaddff, (int) (0xff * alpha))
//                    : withAlpha(0xff555555, (int) (0xff * alpha));
//            guiGraphics.pose().pushMatrix();
//            guiGraphics.pose().translate(seedX + xOffset, this.getY() + this.height - 6);
//            guiGraphics.pose().scale(0.5f, 0.5f);
//            guiGraphics.text(font, "#", 0, 0, seedColor, false);
//            guiGraphics.pose().popMatrix();
//        }

        guiGraphics.disableScissor();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
//        if (record == null || record.seed().isEmpty()) return;
//        if (event.button() != 0) return;
//        if (!seedHovered) return;
//
//        Minecraft.getInstance().keyboardHandler.setClipboard(String.valueOf(record.seed().get()));
//        Minecraft mc = Minecraft.getInstance();
//        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.25f));
//        if (mc.player != null) {
//            mc.player.sendSystemMessage(Component.translatable("scavenger.victory.seed_copied"));
//        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
    }

    private float calculateAlpha(float partialTick) {
        return revealProgress;
    }

    private static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00ffffff);
    }
}
