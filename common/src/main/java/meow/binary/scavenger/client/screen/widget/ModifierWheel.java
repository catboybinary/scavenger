package meow.binary.scavenger.client.screen.widget;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import it.hurts.shatterbyte.shatterlib.util.RenderUtils;
import meow.binary.scavenger.Modifier;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Random;

public class ModifierWheel extends AbstractWidget {
    public static final Identifier SEPARATOR = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/separator.png");
    public static final Identifier MACHINE = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/machine.png");
    public static final Identifier MACHINE_BG = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/machine_bg.png");

    public static final int WHEEL_HEIGHT = 90;
    public static final int SLOT_HEIGHT = 47;

    Random random = new Random();

    public void setRotation(float rotation) {
        int count = Modifier.values().length;

        rotation %= count;
        if (rotation < 0) {
            rotation += count;
        }

        this.rotation = rotation;
    }

    float rotation = 0.5f;
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

        Modifier[] modifiers = Modifier.values();
        int count = modifiers.length;

        int baseIndex = (int)Math.floor(rotation);
        float fraction = rotation - baseIndex;

        float pixelOffset = fraction * SLOT_HEIGHT;

        guiGraphics.enableScissor(this.getX() + 16, this.getY() + 35, this.getX() + 192, this.getY() + 125);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0, pixelOffset);

        int centerY = this.getY() + 80;

        for (int i = -2; i <= 1; i++) {
            int index = Math.floorMod(-baseIndex + i, count);
            Modifier modifier = modifiers[index];

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

            Component name = Component.translatable(
                    "scavenger.modifier." + modifier.name().toLowerCase()
            ).withStyle(ChatFormatting.BOLD);

            Component description = Component.translatable(
                    "scavenger.modifier." + modifier.name().toLowerCase() + ".description"
            );


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
        guiGraphics.pose().popMatrix();
    }

    public void spin() {
        rotationTween.kill();
        rotationTween = Tween.create();
        rotationTween.setTransitionType(TransitionType.CUBIC);
        rotationTween.setEase(EaseType.EASE_OUT);
        rotationTween.tweenMethod(this::setRotation, rotation, rotation + random.nextFloat(30, 30+Modifier.values().length), 5d);
        rotationTween.parallel().tweenRunnable(() -> {
            rotationTween.kill();
            finishingTween.kill();
            finishingTween = Tween.create();
            finishingTween.setTransitionType(TransitionType.BACK);
            finishingTween.setEase(EaseType.EASE_OUT);
            finishingTween.tweenMethod(this::setRotation, rotation, Mth.floor(rotation)+0.5f, 0.5d);
            finishingTween.start();
        }).setDelay(4.5);
        //rotationTween.tweenMethod(this::setDarken, 0f, 1f, 0.4d).setEaseType(EaseType.EASE_IN_OUT).setTransitionType(TransitionType.SINE);
        rotationTween.start();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        this.spin();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
