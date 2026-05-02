package meow.binary.scavenger.client.screen;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.particle.UIParticle;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.client.ScavengerClient;
import meow.binary.scavenger.client.particle.ConfettiUIParticle;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector2f;

import java.util.Random;

import static meow.binary.scavenger.Scavenger.CONFIG;

public class VictoryScreen extends Screen {
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 210;
    private static final int PANEL = 0xdd11151c;
    private static final int BUTTON_WIDTH = 128;
    private static final int BUTTON_HEIGHT = 20;

    private final Random confettiRandom = new Random();
    private final Tween tween = Tween.create();

    private Button disconnectButton;
    private float value = 0;
    private boolean spawnedConfetti;

    private void setValue(float value) {
        this.value = value;
    }

    public VictoryScreen() {
        super(Component.empty());
        tween.tweenMethod(this::setValue, 0f, Mth.PI*2, 4d);
        tween.tweenRunnable(() -> {if (Minecraft.getInstance().screen != this) tween.kill();});
        tween.setLoops(-1);
        tween.start();
    }

    @Override
    protected void init() {
        int panelX = this.width / 2 - PANEL_WIDTH / 2;
        int panelY = this.height / 2 - PANEL_HEIGHT / 2;

        this.disconnectButton = Button.builder(
                        CommonComponents.disconnectButtonLabel(this.minecraft.isLocalServer()),
                        button -> {
                            button.active = false;
                            this.minecraft
                                    .getReportingContext()
                                    .draftReportHandled(this.minecraft, this, () -> this.minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE), true);
                        }
                )
                .bounds(panelX + (PANEL_WIDTH - BUTTON_WIDTH) / 2, panelY + PANEL_HEIGHT - 31, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.disconnectButton);

        if (!spawnedConfetti) {
            spawnedConfetti = true;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.FIREWORK_ROCKET_TWINKLE, 1f));
            spawnConfettiBurst();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackdrop(guiGraphics);

        Font font = Minecraft.getInstance().font;
        int panelX = this.width / 2 - PANEL_WIDTH / 2;
        int panelY = this.height / 2 - PANEL_HEIGHT / 2;

        renderPanel(guiGraphics, panelX, panelY);
        renderTitle(guiGraphics, font, panelY);
        renderResult(guiGraphics, font, panelX, panelY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderBackdrop(GuiGraphics guiGraphics) {
        int accentGlowColor = withAlpha(CONFIG.getVictoryAccentColorArgb(), 0x55);
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);
        guiGraphics.fillGradient(0, 0, this.width, this.height, accentGlowColor, 0x11000000);
    }

    private void renderPanel(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, PANEL);
    }

    private void renderTitle(GuiGraphics guiGraphics, Font font, int panelY) {
        Component victoryText = Component.translatable("scavenger.victory");
        float bob = Mth.cos(value) * 2f;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.width / 2f, panelY + 26 + bob);
        guiGraphics.pose().scale(2f, 2f);
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
        int itemLabelX = panelX + 31;
        int itemLabelY = panelY + 70;
        int itemBoxX = itemLabelX;
        int itemBoxY = panelY + 83;
        int itemBoxSize = 48;
        int rightStartX = panelX + 119;
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
        int modifierValueY = panelY + 142;

        guiGraphics.drawString(font, Component.translatable("scavenger.victory.item_label"), itemLabelX, itemLabelY, 0xff9fdce7, false);
        guiGraphics.drawString(font, Component.translatable("scavenger.victory.time_label"), rightStartX, timerLabelY, 0xff9fdce7, false);
        guiGraphics.fill(itemBoxX, itemBoxY, itemBoxX + itemBoxSize, itemBoxY + itemBoxSize, 0xff000000);
        guiGraphics.fill(timerBoxX, timerBoxY, timerBoxX + timerBoxWidth, timerBoxY + timerBoxHeight, 0xff000000);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(itemBoxX + itemBoxSize/2f, itemBoxY + itemBoxSize/2f);
        guiGraphics.pose().scale(2f, 2f);
        guiGraphics.renderItem(stack, -8, -8);
        guiGraphics.renderItemDecorations(font, stack, -8, -8);
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(timerX, timerY);
        ScavengerClient.renderTimerText(guiGraphics, font, totalSeconds, 0, 0, true, new ShatterColor(accentColor));
        guiGraphics.pose().popMatrix();

        guiGraphics.drawString(font, Component.translatable("scavenger.victory.modifier_label"), rightStartX, modifierLabelY, 0xff9fdce7, false);
        guiGraphics.drawString(font, modifierName, rightStartX, modifierValueY, 0xffffffff, true);
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
    public boolean isPauseScreen() {
        return false;
    }
}
