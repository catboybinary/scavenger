package meow.binary.scavenger.client.screen.widget;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Random;

public class ModifierWheel extends AbstractWidget {
    public static final Identifier SEPARATOR = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/separator.png");
    public static final Identifier MACHINE = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/machine.png");
    public static final Identifier MACHINE_BG = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/machine_bg.png");
    public static final Identifier ARROW_LEFT = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/left_ar.png");
    public static final Identifier ARROW_RIGHT = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/right_ar.png");

    public static final int WHEEL_HEIGHT = 90;
    public static final int SLOT_HEIGHT = 47;

    List<Identifier> modifiers = Modifiers.getIds().stream().filter(id -> !Scavenger.CONFIG.modifierBlacklist.contains(id.toString())).toList();
    List<Identifier> modifiersReversed = modifiers.reversed();

    private boolean isDone;

    public void setRotation(float rotation) {
        int count = modifiers.size();

        rotation %= count;
        if (rotation < 0) {
            rotation += count;
        }

        int currentSegment = Mth.floor(rotation);

        if (lastSegment != currentSegment) {
            lastSegment = currentSegment;
            shouldPlaySound = true;
        }

        this.rotation = rotation;
    }

    boolean shouldPlaySound;
    float rotation = 0.5f;
    int lastSegment = -1;
    Tween rotationTween = Tween.create();
    Tween finishingTween = Tween.create();

    final ScavengerWorldCreateScreen screen;
    float xOffset;

    public void setxOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    public ModifierWheel(int x, int y, int width, int height, ScavengerWorldCreateScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(xOffset, 0);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MACHINE_BG, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        int count = modifiers.size();

        int baseIndex = (int)Math.floor(rotation);
        float fraction = rotation - baseIndex;

        float pixelOffset = fraction * SLOT_HEIGHT;

        guiGraphics.enableScissor(this.getX() + 16, this.getY() + 35, this.getX() + 192, this.getY() + 125);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0, pixelOffset);

        int centerY = this.getY() + 80;

        for (int i = -2; i <= 1; i++) {
            int index = Math.floorMod(-baseIndex + i, count);
            Identifier modifier = modifiersReversed.get(index);

            int y = 35 + i * SLOT_HEIGHT;
            int centerX = this.getX() + this.width / 2;

            float slotCenter = this.getY() + y + pixelOffset + SLOT_HEIGHT / 2f;

            float distance = Math.abs(slotCenter - centerY);

            float maxDist = SLOT_HEIGHT * 1.3f;
            float t = Mth.clamp(distance / maxDist, 0f, 1f);

            float factor = (float)Math.cos(t * Math.PI / 2f);
            factor = 0.5f + factor * 0.5f;
            float xScale = 0.95f + factor * 0.1f;

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(centerX, this.getY() + y + (slotCenter > centerY ? 0 : SLOT_HEIGHT * (1 - factor)));
            guiGraphics.pose().scale(xScale, factor);

            Component name = Modifiers.getName(modifier).withStyle(ChatFormatting.BOLD);
            Component description = Modifiers.getDescription(modifier);


            guiGraphics.drawString(
                    font,
                    name,
                     - font.width(name) / 2,
                    11,
                    0xff491825,
                    false
            );

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(
                    - font.width(description) * 0.75f / 2f,
                    23
            );
            guiGraphics.pose().scale(0.75f);

            guiGraphics.drawString(font, description, 0, 0, 0xff61322e, false);
            guiGraphics.pose().popMatrix();
            guiGraphics.pose().popMatrix();

            int sepY = 35 + i * SLOT_HEIGHT + SLOT_HEIGHT - 6;
            float sepCenter = this.getY() + sepY + pixelOffset + 4;

            float sepDistance = Math.abs(sepCenter - centerY);
            float sepT = Mth.clamp(sepDistance / maxDist, 0f, 1f);

            float sepFactor = (float)Math.cos(sepT * Math.PI / 2f);
            sepFactor = 0.5f + sepFactor * 0.5f;

            float sepXScale = 0.95f + sepFactor * 0.1f;

            guiGraphics.pose().pushMatrix();

            guiGraphics.pose().translate(
                    centerX,
                    this.getY() + sepY + (sepCenter > centerY ? -8 * (1 - sepFactor) : 8 * (1 - sepFactor))
            );

            guiGraphics.pose().scale(sepXScale, sepFactor);

            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    SEPARATOR,
                    -88,
                    0,
                    0,
                    0,
                    176,
                    8,
                    176,
                    8
            );

            guiGraphics.pose().popMatrix();
        }

        guiGraphics.pose().popMatrix();

        guiGraphics.disableScissor();

        guiGraphics.fillGradient(this.getX() + 16, this.getY() + 35, this.getX() + 192, this.getY() + 55, 0xaa59443c, 0x0);
        guiGraphics.fillGradient(this.getX() + 16, this.getY() + 105, this.getX() + 192, this.getY() + 125, 0x0, 0xaa59443c);

        //guiGraphics.hLine(this.getX(), this.getX()+this.width, this.getY() + 80, 0xffff0000);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MACHINE, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ARROW_LEFT, this.getX()+1, this.getY()+72, 0, 0, 21, 16, 21, 16);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ARROW_RIGHT, this.getX()+this.width-21-1, this.getY()+72, 0, 0, 21, 16, 21, 16);

        //guiGraphics.drawString(font, this.getCurrentModifier().getPath(), this.getX() + this.width, this.getY(), 0xffffffff, true);

        guiGraphics.pose().popMatrix();

        if (shouldPlaySound) {
            shouldPlaySound = false;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 2f));
        }
    }

    private void finish() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1f));
        this.isDone = true;
        this.screen.setChosenModifier(this.getCurrentModifier());
        this.screen.createWidget.active = true;
    }

    private Identifier getCurrentModifier() {
        if (lastSegment >= 0 && lastSegment < modifiers.size()) {
            int index = Math.floorMod(lastSegment - 1, modifiers.size());
            return modifiers.get(index);
        }

        return Modifiers.NONE.getId();
    }

    public void spin() {
        rotationTween.kill();
        rotationTween = Tween.create();
        rotationTween.setTransitionType(TransitionType.CUBIC);
        rotationTween.setEase(EaseType.EASE_OUT);
        rotationTween.tweenMethod(this::setRotation, rotation, rotation + 1, 1d);
        rotationTween.parallel().tweenRunnable(() -> {
            rotationTween.kill();
            finishingTween.kill();
            finishingTween = Tween.create();
            finishingTween.setTransitionType(TransitionType.BACK);
            finishingTween.setEase(EaseType.EASE_OUT);
            finishingTween.tweenRunnable(() -> Minecraft.getInstance().submit(this::finish));
            finishingTween.tweenMethod(this::setRotation, rotation, Mth.floor(rotation)+0.5f, 0.5d);
            finishingTween.start();
        }).setDelay(0.5);
        //rotationTween.tweenMethod(this::setDarken, 0f, 1f, 0.4d).setEaseType(EaseType.EASE_IN_OUT).setTransitionType(TransitionType.SINE);
        rotationTween.start();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        //if (isDone) return;

        isDone = false;
        screen.createWidget.active = false;

        this.spin();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
