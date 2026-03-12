package meow.binary.scavenger.client.screen.widget;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import it.hurts.shatterbyte.shatterlib.client.particle.ParticleSystem;
import it.hurts.shatterbyte.shatterlib.client.particle.UIParticle;
import it.hurts.shatterbyte.shatterlib.util.AnimationUtils;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.particle.ConfettiUIParticle;
import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ItemWheel extends AbstractWidget {
    public static final Identifier WHEEL_METAL = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/wheel_metal.png");
    public static final Identifier WHEEL_WOOD = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/wheel_wood.png");
    public static final Identifier WHEEL_TR_LEFT = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/wheel_tr_left.png");
    public static final Identifier WHEEL_TR_RIGHT = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/wheel_tr_right.png");
    public static final Identifier ARROW = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/gui/arrow.png");
    public static final double QUARTER_PI = Math.PI / 4d;

    public static final RenderPipeline MULTIPLIED_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withBlend(new BlendFunction(SourceFactor.DST_COLOR, DestFactor.ONE))
            .withColorWrite(true)
            .withLocation(Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "multiply"))
            .build();

    boolean shouldPlaySound;
    private int lastSegment = -1;

    public void setxOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    private float xOffset = 0;

    final ScavengerWorldCreateScreen screen;

    public boolean isDone;

    public void setDarken(float darken) {
        this.darken = darken;
    }

    float darken = 0;

    Random random = new Random();
    ArrayList<Item> items = new ArrayList<>();
    Tween rotationTween = Tween.create();
    float rotation;

    public ItemWheel(int x, int y, int width, int height, ScavengerWorldCreateScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
        List<Item> allItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .collect(Collectors.toList());

        Collections.shuffle(allItems);

        items.addAll(allItems.subList(0, Math.min(8, allItems.size())));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //RenderUtils.renderOutline(guiGraphics, this.getX(), this.getY(), this.width, this.height, 0xffff0000);
        int currentSegment = lastSegment;
        int woodColor = AnimationUtils.COLOR.lerp(new ShatterColor(0xffffffff), new ShatterColor(0xff999999), darken).getARGB();
        int selectionColor = AnimationUtils.COLOR.lerp(new ShatterColor(0xff000000), new ShatterColor(0xffbbbbbb), darken).getARGB();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(xOffset, 0);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f);
        guiGraphics.pose().rotate(rotation);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, WHEEL_WOOD, -105, -105, 0, 0, 210, 210, 210, 210, woodColor);
        if (isDone) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().rotate(-Mth.HALF_PI * ((currentSegment + 1) / 2));
            guiGraphics.blit(MULTIPLIED_PIPELINE, currentSegment % 2 == 0 ? WHEEL_TR_LEFT : WHEEL_TR_RIGHT, -105, -105, 0, 0, 210, 210, 210, 210, selectionColor);
            guiGraphics.pose().popMatrix();
        }

        guiGraphics.pose().rotate((float) (QUARTER_PI / 2f));
        for (Item item : items.reversed()) {
            guiGraphics.renderItem(item.getDefaultInstance(), -8, -68);
            guiGraphics.pose().rotate((float) QUARTER_PI);
        }
        guiGraphics.pose().popMatrix();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, WHEEL_METAL, this.getX(), this.getY(), 0, 0, 210, 210, 210, 210);

        float arrowTilt = (float) (Math.max(0.65f, rotation % (QUARTER_PI)) - 0.65f) * 1.5f;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.getX() + this.width / 2f, this.getY() + 13);
        guiGraphics.pose().rotate(-arrowTilt);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ARROW, -15, -14, 0, 0, 30, 28, 30, 28);
        guiGraphics.pose().popMatrix();

//        guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(lastSegment), this.getX(), this.getY() + 18, 0xffffffff);
//        guiGraphics.renderItem(this.getCurrentItem().getDefaultInstance(), this.getX(), this.getY());

        if (shouldPlaySound) {
            shouldPlaySound = false;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 2f));
        }

        guiGraphics.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    private void setRotation(float value) {
        float fullRotation = (float) (value % (Math.PI * 2d));
        if (fullRotation < 0) fullRotation += (float)(Math.PI * 2d);

        this.rotation = fullRotation;

        int currentSegment = (int)(fullRotation / QUARTER_PI);

        if (currentSegment != lastSegment) {
            lastSegment = currentSegment;
            shouldPlaySound = true;
        }
    }

    private Item getCurrentItem() {
        if (lastSegment >= 0 && lastSegment < items.size()) {
            return items.get(lastSegment);
        }

        return Items.AIR;
    }

    private void finish() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1f));
        this.isDone = true;
        this.screen.setChosenItem(this.getCurrentItem());

        for (int i = 0; i < 100; i++) {
            float direction = random.nextFloat(-0.3f, 0.3f);
            ConfettiUIParticle particle = new ConfettiUIParticle(
                    random.nextFloat(8,16) - (0.3f - Math.abs(direction)) * 2, random.nextInt(30, 60),
                    this.getX() + this.width / 2f + random.nextInt(-2, 2),
                    this.getY() + this.height / 2f + random.nextInt(-10, 10) - 5,
                    UIParticle.Layer.SCREEN, 1
            );
            ShatterColor color = ShatterColor.fromHSV(random.nextFloat(), 1f, 1f, 1f);


            particle.getTransform().setSize(new Vector2f(1, 1).mul(random.nextFloat(0.75f, 1.5f)));
            particle.getTransform().setRoll(random.nextFloat(-180,180));
            particle.setFriction(random.nextFloat(0.01f,0.04f));
            particle.setRollVelocity(random.nextFloat(-1, 1));
            particle.setDirection(direction, -1);
            particle.setColors(color, color, color.multiply(1f, 1f, 1f, 0f));
            particle.setScreen(this.screen);
            particle.getTransform().updateOldValues();
            particle.instantiate();
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        //if (isDone) return;

        isDone = false;
        darken = 0f;
        this.screen.setChosenItem(Items.AIR);

        rotationTween.kill();
        rotationTween = Tween.create();
        rotationTween.setTransitionType(TransitionType.QUART);
        rotationTween.setEase(EaseType.EASE_OUT);
        rotationTween.tweenMethod(this::setRotation, rotation, rotation + random.nextFloat((float) (Math.PI * 8), (float) (Math.PI * 10)), 10d);
        rotationTween.tweenRunnable(() -> Minecraft.getInstance().submit(this::finish));
        rotationTween.tweenMethod(this::setDarken, 0f, 1f, 0.4d).setEaseType(EaseType.EASE_IN_OUT).setTransitionType(TransitionType.SINE);
        rotationTween.start();
    }
}
