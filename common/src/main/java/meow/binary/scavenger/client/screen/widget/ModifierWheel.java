package meow.binary.scavenger.client.screen.widget;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
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

import java.util.Random;

public class ModifierWheel extends AbstractWidget {
    public static final Identifier SLOT = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/slot.png");
    public static final Identifier MACHINE = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/machine.png");

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

        Modifier[] modifiers = Modifier.values();
        int count = modifiers.length;

        int baseIndex = (int)Math.floor(rotation);
        float fraction = rotation - baseIndex;

        float pixelOffset = fraction * SLOT_HEIGHT;

        guiGraphics.enableScissor(this.getX() + 16, this.getY() + 35, this.getX() + 192, this.getY() + 125);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0, pixelOffset);

        for (int i = -1; i <= 1; i++) {
            int index = Math.floorMod(-baseIndex + i, count);
            Modifier modifier = modifiers[index];

            int y = 35 + i * SLOT_HEIGHT;

            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    SLOT,
                    this.getX()+16,
                    this.getY()+y,
                    0, 0,
                    176, SLOT_HEIGHT,
                    176, SLOT_HEIGHT
            );

            Component name = Component.translatable(
                    "scavenger.modifier." + modifier.name().toLowerCase()
            ).withStyle(ChatFormatting.BOLD);

            Component description = Component.translatable(
                    "scavenger.modifier." + modifier.name().toLowerCase() + ".description"
            );

            int centerX = this.getX() + this.width / 2;

            guiGraphics.drawString(
                    font,
                    name,
                    centerX - font.width(name) / 2,
                    this.getY() + y + 11,
                    0xff491825,
                    false
            );

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(
                    centerX - font.width(description) * 0.75f / 2f,
                    this.getY() + y + 23
            );
            guiGraphics.pose().scale(0.75f);

            guiGraphics.drawString(font, description, 0, 0, 0xff61322e, false);
            guiGraphics.pose().popMatrix();
        }

        guiGraphics.pose().popMatrix();

        guiGraphics.disableScissor();

        guiGraphics.fillGradient(this.getX() + 16, this.getY() + 35, this.getX() + 192, this.getY() + 48, 0xcc765d54, 0x0);
        guiGraphics.fillGradient(this.getX() + 16, this.getY() + 112, this.getX() + 192, this.getY() + 125, 0x0, 0xcc765d54);

        //guiGraphics.hLine(this.getX(), this.getX()+this.width, this.getY() + 80, 0xffff0000);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MACHINE, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        guiGraphics.pose().popMatrix();
    }

    public void spin() {
        rotationTween.kill();
        rotationTween = Tween.create();
        rotationTween.setTransitionType(TransitionType.QUART);
        rotationTween.setEase(EaseType.EASE_OUT);
        rotationTween.tweenMethod(this::setRotation, rotation, rotation + random.nextFloat(7, 13), 10d);
        //rotationTween.tweenRunnable(() -> Minecraft.getInstance().submit(this::finish));
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
