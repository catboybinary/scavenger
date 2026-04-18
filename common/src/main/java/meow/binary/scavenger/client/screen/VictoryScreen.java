package meow.binary.scavenger.client.screen;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.particle.UIParticle;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.client.ScavengerClient;
import meow.binary.scavenger.client.particle.ConfettiUIParticle;
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

public class VictoryScreen extends Screen {
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 178;
    private static final int ACCENT = 0xff11d0f0;
    private static final int ACCENT_DARK = 0xff0a6f86;
    private static final int PANEL = 0xdd11151c;
    private static final int PANEL_INNER = 0xee1a222b;

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
        tween.setLoops(-1);
        tween.start();
    }

    @Override
    protected void init() {
        this.disconnectButton = Button.builder(
                        CommonComponents.disconnectButtonLabel(this.minecraft.isLocalServer()),
                        button -> {
                            button.active = false;
                            this.minecraft
                                    .getReportingContext()
                                    .draftReportHandled(this.minecraft, this, () -> this.minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE), true);
                        }
                )
                .bounds(this.width / 2 - 64, this.height / 2 + 64, 128, 20)
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
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x5511d0f0, 0x11000000);

        int glow = 28 + (int) (Mth.sin(value) * 6);
        int cx = this.width / 2;
        int cy = this.height / 2;
        //guiGraphics.fill(cx - 170, cy - glow, cx + 170, cy + glow, 0x2211d0f0);
        guiGraphics.fill(cx - 118, 0, cx + 118, this.height, 0x33000000);
    }

    private void renderPanel(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 5, y - 5, x + PANEL_WIDTH + 5, y + PANEL_HEIGHT + 5, 0x55000000);
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, PANEL);
        guiGraphics.fill(x + 6, y + 6, x + PANEL_WIDTH - 6, y + PANEL_HEIGHT - 6, PANEL_INNER);
        guiGraphics.renderOutline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xffffffff);
        guiGraphics.renderOutline(x + 3, y + 3, PANEL_WIDTH - 6, PANEL_HEIGHT - 6, ACCENT_DARK);

        guiGraphics.fill(x + 16, y + 52, x + PANEL_WIDTH - 16, y + 54, ACCENT);
        guiGraphics.fill(x + 16, y + 122, x + PANEL_WIDTH - 16, y + 124, 0x66ffffff);
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

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.width / 2f, panelY + 84);
        guiGraphics.pose().scale(3f, 3f);
        guiGraphics.renderItem(stack, -8, -8);
        guiGraphics.renderItemDecorations(font, stack, -8, -8);
        guiGraphics.pose().popMatrix();

        guiGraphics.drawCenteredString(font, stack.getHoverName(), this.width / 2, panelY + 108, 0xffffffff);
        guiGraphics.drawCenteredString(font, Component.translatable("scavenger.item_to_find"), this.width / 2, panelY + 64, 0xff9fdce7);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(panelX + 62, panelY + 138);
        ScavengerClient.renderTimerText(guiGraphics, font, totalSeconds, 0, 0, true, new ShatterColor(ACCENT));
        guiGraphics.pose().popMatrix();
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
