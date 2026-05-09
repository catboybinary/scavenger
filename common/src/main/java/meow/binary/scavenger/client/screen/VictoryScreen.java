package meow.binary.scavenger.client.screen;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import it.hurts.shatterbyte.shatterlib.client.particle.UIParticle;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.client.ScavengerClient;
import meow.binary.scavenger.client.particle.ConfettiUIParticle;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector2f;

import java.util.List;
import java.util.Random;

import static meow.binary.scavenger.Scavenger.CONFIG;

public class VictoryScreen extends Screen {
    private static final int PANEL_WIDTH = 246;
    private static final int PANEL_HEIGHT = 216;
    private static final int BUTTON_WIDTH = 128;
    private static final int BUTTON_HEIGHT = 20;
    private static final Identifier FULL = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/victory/full.png");
    private static final float ITEM_NAME_SCALE = 0.66f;
    private static final int ITEM_NAME_LINE_STEP = 10;
    private static final long ITEM_NAME_SCROLL_PERIOD_MS = 5000L;
    private static final float ITEM_NAME_SCROLL_HOLD_RATIO = 0.22f;

    private final Random confettiRandom = new Random();
    private final Tween tween = Tween.create();
    private final Tween sizeTween = Tween.create();

    private Button disconnectButton;
    private float value = 0;
    private boolean spawnedConfetti;
    private Component seedText = Component.empty();
    private String seedValue = "";
    private int seedX;
    private int seedY;
    private int seedWidth;
    private int seedHeight;

    private float size = 0f;

    private void setValue(float value) {
        this.value = value;
    }

    public VictoryScreen() {
        super(Component.empty());
        tween.tweenMethod(this::setValue, 0f, Mth.PI*2, 4d);
        tween.tweenRunnable(() -> {if (Minecraft.getInstance().screen != this) tween.kill();});
        tween.setLoops(-1);
        tween.start();

        sizeTween.tweenMethod(this::setSize, 0f, 1f, 1f).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.EXPO);
        sizeTween.start();
    }

    @Override
    protected void init() {
        int panelX = this.width / 2 - PANEL_WIDTH / 2;
        int panelY = this.height / 2 - PANEL_HEIGHT / 2;
        Font font = Minecraft.getInstance().font;

        this.seedValue = resolveWorldSeed();
        this.seedText = Component.translatable("scavenger.victory.seed", this.seedValue);
        this.seedWidth = font.width(this.seedText);
        this.seedHeight = font.lineHeight;
        this.seedX = panelX + (PANEL_WIDTH - this.seedWidth) / 2;
        this.seedY = panelY + PANEL_HEIGHT - 45;

        this.disconnectButton = Button.builder(
                        CommonComponents.disconnectButtonLabel(this.minecraft.isLocalServer()),
                        button -> {
                            button.active = false;
                            this.minecraft
                                    .getReportingContext()
                                    .draftReportHandled(this.minecraft, this, () -> this.minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE), true);
                        }
                )
                .bounds(panelX + (PANEL_WIDTH - BUTTON_WIDTH) / 2, panelY + PANEL_HEIGHT - 33, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.disconnectButton);

        if (!spawnedConfetti) {
            spawnedConfetti = true;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VAULT_OPEN_SHUTTER, 1f));
            //spawnConfettiBurst();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackdrop(guiGraphics);

        Font font = Minecraft.getInstance().font;
        int panelX = this.width / 2 - PANEL_WIDTH / 2;
        int panelY = this.height / 2 - PANEL_HEIGHT / 2;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.width/2f, this.height/2f);
        guiGraphics.pose().scale(size);
        guiGraphics.pose().translate(-this.width/2f, -this.height/2f);

        renderPanel(guiGraphics, panelX, panelY);
        renderTitle(guiGraphics, font, panelY+8);
        renderResult(guiGraphics, font, panelX, panelY);
        renderSeed(guiGraphics, font, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.pose().popMatrix();
    }


    private void renderBackdrop(GuiGraphics guiGraphics) {
        int accentGlowColor = withAlpha(0xfffdcd23, (int) (0x55*size));
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);
        guiGraphics.fillGradient(0, 0, this.width, this.height, accentGlowColor, 0x11000000);
    }

    private void renderPanel(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FULL, x, y, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);
    }

    private void renderTitle(GuiGraphics guiGraphics, Font font, int panelY) {
        Component victoryText = Component.translatable("scavenger.victory");
        float bob = Mth.cos(value) * 2f;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.width / 2f, panelY + 26 + bob);
        guiGraphics.pose().scale(1.5f, 1.5f);
        guiGraphics.pose().rotate(Mth.sin(value) / 12f);
        guiGraphics.drawString(font, victoryText, -font.width(victoryText) / 2, -4, 0xfff8ffff, true);
        guiGraphics.pose().popMatrix();
    }

    private void renderResult(GuiGraphics guiGraphics, Font font, int panelX, int panelY) {
        Level level = Minecraft.getInstance().level;
        float tickrate = level == null ? 20f : level.tickRateManager().tickrate();
        double ticks = ClientScavengerData.winTimestamp;
        double totalSeconds = ticks / tickrate;
        ItemStack stack = ClientScavengerData.item.getDefaultInstance();
        Component modifierName = Modifiers.getName(ClientScavengerData.modifier);
        int accentColor = CONFIG.getVictoryAccentColorArgb();
        int itemLabelX = panelX + 37;
        int itemLabelY = panelY + 70;
        int itemBoxX = itemLabelX;
        int itemBoxY = panelY + 76;
        int itemBoxSize = 56;
        int rightStartX = panelX + 122;
        int timerLabelY = panelY + 70;
        int hours = (int)(totalSeconds / 3600);
        int minutes = (int)((totalSeconds % 3600) / 60);
        int seconds = (int)(totalSeconds % 60);
        int millis = (int)((totalSeconds - Math.floor(totalSeconds)) * 100);
        String time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        String ms = String.format(".%02d", millis);
        int timerTextWidth = font.width(time) * 2 + font.width(ms);
        int timerBoxPaddingX = 5;
        int timerBoxPaddingTop = 5;
        int timerBoxPaddingBottom = 4;
        int timerBoxX = rightStartX;
        int timerBoxY = itemBoxY;
        int timerX = timerBoxX+timerBoxPaddingX;
        int timerY = timerBoxY+timerBoxPaddingTop;
        int timerBoxWidth = timerTextWidth + timerBoxPaddingX * 2;
        int timerBoxHeight = 16 + timerBoxPaddingTop + timerBoxPaddingBottom;
        int modifierLabelY = panelY + 126;
        //guiGraphics.drawString(font, Component.translatable("scavenger.victory.item_label"), itemLabelX, itemLabelY, 0xff9fdce7, false);
        //guiGraphics.drawString(font, Component.translatable("scavenger.victory.time_label"), rightStartX, timerLabelY, 0xff9fdce7, false);
        //guiGraphics.fill(itemBoxX, itemBoxY, itemBoxX + itemBoxSize, itemBoxY + itemBoxSize, 0xff000000);
        //guiGraphics.fill(timerBoxX, timerBoxY, timerBoxX + timerBoxWidth, timerBoxY + timerBoxHeight, 0xff000000);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(itemBoxX + itemBoxSize/2f, itemBoxY + itemBoxSize/2f);
        guiGraphics.pose().scale(2f, 2f);
        guiGraphics.renderItem(stack, -8, -8);
        guiGraphics.renderItemDecorations(font, stack, -8, -8);
        guiGraphics.pose().popMatrix();

        int itemNameBoxX = panelX + 39;
        int itemNameBoxY = panelY + 144;
        int itemNameBoxWidth = 52;
        int itemNameBoxHeight = 14;

        renderScrollingTextBox(guiGraphics, font, stack.getHoverName(), itemNameBoxX, itemNameBoxY, itemNameBoxWidth, itemNameBoxHeight, 0xffffffff);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(timerX, timerY);
        ScavengerClient.renderTimerText(guiGraphics, font, totalSeconds, 0, 0, true, new ShatterColor(0xffffe6c8));
        guiGraphics.pose().popMatrix();


        int modifierCenterX = panelX+166;
        int modifierCenterY = panelY+135;
        //guiGraphics.drawString(font, Component.translatable("scavenger.victory.modifier_label"), rightStartX, modifierLabelY, 0xff9fdce7, false);
        List<FormattedCharSequence> sequences = font.split(modifierName, 100);
        int modifierValueY = modifierCenterY - 5 * (sequences.size() - 1);

        for (FormattedCharSequence sequence : sequences) {
            guiGraphics.drawString(font, sequence, modifierCenterX - font.width(sequence)/2, modifierValueY, 0xff7e3926, false);
            modifierValueY+=10;
        }
    }

    private void renderSeed(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        boolean hovered = isMouseOverSeed(mouseX, mouseY);
        int color = hovered ? 0xffffffff : 0xffffe6c8;
        guiGraphics.drawString(font, this.seedText, this.seedX, this.seedY, color, true);
    }

    private void renderScrollingTextBox(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int width, int height, int color) {
        List<FormattedCharSequence> lines = font.split(text, (int) (width / ITEM_NAME_SCALE));
        float visibleHeight = height / ITEM_NAME_SCALE;
        float contentHeight = lines.isEmpty() ? 0 : (lines.size() - 1) * ITEM_NAME_LINE_STEP + font.lineHeight;
        float scrollOffset = 0f;
        float startY;

        if (contentHeight <= visibleHeight) {
            startY = -contentHeight / 2f;
        } else {
            float overflow = contentHeight - visibleHeight;
            float phase = (System.currentTimeMillis() % ITEM_NAME_SCROLL_PERIOD_MS) / (float) ITEM_NAME_SCROLL_PERIOD_MS;
            float triangle = getHeldTrianglePhase(phase, ITEM_NAME_SCROLL_HOLD_RATIO);
            startY = -visibleHeight / 2f;
            scrollOffset = -overflow * triangle;
        }

        guiGraphics.enableScissor(x, y, x + width, y + height);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x + width / 2f, y + height / 2f);
        guiGraphics.pose().scale(ITEM_NAME_SCALE);
        guiGraphics.pose().translate(0f, scrollOffset);

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int lineX = -font.width(line) / 2;
            int lineY = Mth.floor(startY + i * ITEM_NAME_LINE_STEP);
            guiGraphics.drawString(font, line, lineX, lineY, color, true);
        }

        guiGraphics.pose().popMatrix();
        guiGraphics.disableScissor();
    }

    private static float getHeldTrianglePhase(float phase, float holdRatio) {
        float clampedHold = Mth.clamp(holdRatio, 0f, 0.49f);
        float moveSpan = 0.5f - clampedHold;

        if (phase < clampedHold) {
            return 0f;
        }

        if (phase < 0.5f) {
            return (phase - clampedHold) / moveSpan;
        }

        if (phase < 0.5f + clampedHold) {
            return 1f;
        }

        return 1f - (phase - 0.5f - clampedHold) / moveSpan;
    }

    private String resolveWorldSeed() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hasSingleplayerServer()) {
            return Long.toString(minecraft.getSingleplayerServer().overworld().getSeed());
        }

        return "?";
    }

    private boolean isMouseOverSeed(double mouseX, double mouseY) {
        return mouseX >= this.seedX && mouseX < this.seedX + this.seedWidth
                && mouseY >= this.seedY && mouseY < this.seedY + this.seedHeight;
    }

    private void spawnConfettiBurst() {
        int originX = this.width / 2;
        int originY = this.height / 2 - 72;

        for (int i = 0; i < 140; i++) {
            float direction = confettiRandom.nextFloat(-0.9f, 0.9f);
            ConfettiUIParticle particle = new ConfettiUIParticle(
                    confettiRandom.nextFloat(7f, 18f),
                    confettiRandom.nextInt(45, 90),
                    originX + confettiRandom.nextInt(-48, 49),
                    originY + confettiRandom.nextInt(-8, 16),
                    UIParticle.Layer.SCREEN,
                    1
            );

            ShatterColor color = ShatterColor.fromHSV(0.15f * confettiRandom.nextInt(7), 0.9f, 1f, 1f);
            particle.getTransform().setSize(new Vector2f(1, 1).mul(confettiRandom.nextFloat(0.8f, 1.8f)));
            particle.getTransform().setRoll(confettiRandom.nextFloat(-30f, 30f));
            particle.setFriction(confettiRandom.nextFloat(0.01f, 0.04f));
            particle.setRollVelocity(confettiRandom.nextFloat(-1.2f, 1.2f));
            particle.setDirection(direction, -1);
            particle.setColors(color, color, color.multiply(1f, 1f, 1f, 0f));
            particle.setScreen(this);
            particle.getTransform().updateOldValues();
            particle.instantiate();
        }
    }

    private static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00ffffff);
    }

    @Override
    public void onClose() {
        super.onClose();
        tween.kill();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0 && isMouseOverSeed(event.x(), event.y()) && !this.seedValue.isEmpty() && !"?".equals(this.seedValue)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.seedValue);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.25f));
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.translatable("scavenger.victory.seed_copied"), true);
            }
            return true;
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
