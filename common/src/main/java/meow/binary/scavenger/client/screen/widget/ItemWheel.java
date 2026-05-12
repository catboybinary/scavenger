package meow.binary.scavenger.client.screen.widget;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import dev.architectury.platform.Platform;
import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import it.hurts.shatterbyte.shatterlib.client.particle.UIParticle;
import it.hurts.shatterbyte.shatterlib.util.AnimationUtils;
import it.hurts.shatterbyte.shatterlib.util.RenderUtils;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.particle.StarUIParticle;
import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
    private float yOffset = 0;

    final ScavengerWorldCreateScreen screen;

    public boolean isDone;
    private boolean rolling;

    public void setDarken(float darken) {
        this.darken = darken;
    }

    float darken = 0;

    Random confettiRandom = new Random();
    Random sparkleRandom = new Random();
    NonNullList<Item> items = NonNullList.withSize(8, Items.BARRIER);
    Tween rotationTween = Tween.create();
    float rotation;

    Tween scaleTween = Tween.create();

    private float itemScale = 0f;

    public ItemWheel(int x, int y, int width, int height, ScavengerWorldCreateScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
        Set<String> configuredItems = Scavenger.CONFIG.gameplay.rollableItems == null ? Set.of() : Scavenger.CONFIG.gameplay.rollableItems.stream()
                .filter(itemId -> itemId != null && !itemId.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());

        List<Item> allItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .filter(item -> !isDefaultExcludedItem(item))
                .filter(item -> {
                    if (configuredItems.isEmpty()) {
                        return true;
                    }

                    boolean isConfigured = configuredItems.contains(item.arch$registryName().toString());
                    return Scavenger.CONFIG.gameplay.rollableItemsIsBlacklist != isConfigured;
                })
                .collect(Collectors.toList());

        if (allItems.isEmpty()) {
            allItems = BuiltInRegistries.ITEM.stream()
                    .filter(item -> item != Items.AIR)
                    .filter(item -> !isDefaultExcludedItem(item))
                    .collect(Collectors.toList());
        }

        Collections.shuffle(allItems, screen.random);

        for (int i = 0; !allItems.isEmpty() && i < items.size(); i++) {
            items.set(i, allItems.get(i % allItems.size()));
        }
    }

    private static boolean isDefaultExcludedItem(Item item) {
        return item.getDefaultInstance().is(Scavenger.UNROLLABLE_BY_DEFAULT);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //RenderUtils.renderOutline(guiGraphics, this.getX(), this.getY(), this.width, this.height, 0xffff0000);
        int currentSegment = lastSegment;
        int woodColor = AnimationUtils.COLOR.lerp(new ShatterColor(0xffffffff), new ShatterColor(0xff999999), darken).getARGB();
        int selectionColor = AnimationUtils.COLOR.lerp(new ShatterColor(0xff000000), new ShatterColor(0xffbbbbbb), darken).getARGB();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(xOffset, yOffset);

        if (Scavenger.CONFIG.wheels.scaleItemWheel != 1f) {
            guiGraphics.pose().translate(this.getX()+this.width/2f, this.getY()+this.height/2f);
            guiGraphics.pose().scale(Scavenger.CONFIG.wheels.scaleItemWheel);
            guiGraphics.pose().translate(-this.getX()-this.width/2f, -this.getY()-this.height/2f);
        }

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

        if (itemScale > 0.05) {
            Font font = Minecraft.getInstance().font;
            ItemStack stack = this.getCurrentItem().getDefaultInstance();
            guiGraphics.pose().translate(this.width / 2f + this.getX(), this.height / 2f + this.getY());
            guiGraphics.pose().scale(itemScale);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/shadow.png"), -16, -16, 0, 0, 32, 32, 32, 32, 0xffffffff);
            guiGraphics.renderItem(stack, -8, -8);
            Component name = stack.getStyledHoverName();
            List<FormattedCharSequence> list = font.split(name, 192/2);
            guiGraphics.pose().scale(0.5f);
            int y = 20;
            for (FormattedCharSequence sequence : list) {
                guiGraphics.drawString(font, sequence, -font.width(sequence)/2, y, 0xffffffff, true);
                y+=10;
            }
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
            spawnArrowSparkles();
        }
    }

    private void spawnArrowSparkles() {
        float scale = Scavenger.CONFIG.wheels.scaleItemWheel;
        float pivotX = this.getX() + this.width / 2f;
        float pivotY = this.getY() + this.height / 2f;
        float tipX = this.getX() + this.width / 2f;
        float tipY = this.getY() + 26f;

        if (scale != 1f) {
            tipX = pivotX + (tipX - pivotX) * scale;
            tipY = pivotY + (tipY - pivotY) * scale;
        }

        tipX += xOffset;
        tipY += yOffset;

        for (int i = 0; i < 3; i++) {
            StarUIParticle particle = new StarUIParticle(
                    sparkleRandom.nextFloat(0.75f, 1.75f),
                    sparkleRandom.nextInt(10, 16),
                    tipX + sparkleRandom.nextFloat(-5f, 5f),
                    tipY + sparkleRandom.nextFloat(-2f, 4f),
                    UIParticle.Layer.SCREEN,
                    2
            );

            ShatterColor color = new ShatterColor(0xfff8e7b5);
            particle.setRenderPipeline(UIParticle.ADDITIVE_PIPELINE);
            particle.getTransform().setSize(new Vector2f(1, 1).mul(sparkleRandom.nextFloat(0.65f, 1.15f)));
            particle.getTransform().setRoll(sparkleRandom.nextFloat(-10f, 10f));
            particle.setGravity(0.33f);
            particle.setFriction(sparkleRandom.nextFloat(0.004f, 0.014f));
            particle.setRollVelocity(sparkleRandom.nextFloat(-0.45f, 0.45f));
            particle.setDirection(sparkleRandom.nextFloat(-1f, 1f), sparkleRandom.nextFloat(-1f, 1f));
            particle.setColors(color, color);
            particle.setScreen(this.screen);
            particle.getTransform().updateOldValues();
            particle.instantiate();
        }
    }

    private Item getCurrentItem() {
        if (lastSegment >= 0 && lastSegment < items.size()) {
            return items.get(lastSegment);
        }

        return Items.AIR;
    }

    private void finish() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VAULT_OPEN_SHUTTER, 1f));
        this.isDone = true;
        this.rolling = false;
        this.screen.setChosenItem(this.getCurrentItem());

        if (!Scavenger.CONFIG.wheels.removeItemReveal) {
            this.revealItem();
        }

//        for (int i = 0; i < 100; i++) {
//            float direction = confettiRandom.nextFloat(-1f, 1f);
//            StarUIParticle particle = new StarUIParticle(
//                    confettiRandom.nextFloat(0.15f, 0.45f), confettiRandom.nextInt(35, 55),
//                    this.getX() + this.width / 2f + confettiRandom.nextInt(-30, 31),
//                    this.getY() + this.height / 2f + confettiRandom.nextInt(-30, 31),
//                    UIParticle.Layer.SCREEN, 0
//            );
//
//            ShatterColor color = ShatterColor.fromHSV(0.12f + 0.05f * confettiRandom.nextFloat(), 0.35f, 1f, 1f);
//
//            particle.getTransform().setSize(new Vector2f(1, 1).mul(confettiRandom.nextFloat(0.9f, 1.35f)));
//            particle.getTransform().setRoll(confettiRandom.nextFloat(-12f, 12f));
//            particle.setFriction(confettiRandom.nextFloat(0.005f, 0.015f));
//            particle.setRollVelocity(confettiRandom.nextFloat(-0.15f, 0.15f));
//            particle.setDirection(direction, confettiRandom.nextFloat(-0.2f, 0.2f));
//            particle.setColors(color, color, color.multiply(1f, 1f, 1f, 0f));
//            particle.setScreen(this.screen);
//            particle.getTransform().updateOldValues();
//            particle.instantiate();
//        }
    }

    private void revealItem() {
        scaleTween.kill();
        scaleTween = Tween.create();
        scaleTween.setTransitionType(TransitionType.CUBIC);
        scaleTween.tweenMethod(this::setItemScale, 0f, 3f, 0.75d).setEaseType(EaseType.EASE_OUT);
        scaleTween.tweenInterval(1);
        scaleTween.tweenMethod(this::setItemScale, 3f, 0f, 0.75).setEaseType(EaseType.EASE_IN);
        scaleTween.start();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        this.trySpin();
    }

    public boolean trySpin() {
        if ((isDone || rolling) && !Platform.isDevelopmentEnvironment()) {
            return false;
        }

        isDone = false;
        rolling = true;
        darken = 0f;
        scaleTween.kill();
        scaleTween = Tween.create();
        scaleTween.tweenMethod(this::setItemScale, 0f, 0f, 0);
        scaleTween.start();
        this.screen.setChosenItem(Items.AIR);

        rotationTween.kill();
        rotationTween = Tween.create();
        rotationTween.setTransitionType(TransitionType.QUART);
        rotationTween.setEase(EaseType.EASE_OUT);
        rotationTween.tweenMethod(this::setRotation, rotation, rotation + screen.random.nextFloat((float) (Math.PI * 8), (float) (Math.PI * 10)), Scavenger.CONFIG.wheels.itemRollTime);
        rotationTween.tweenRunnable(() -> Minecraft.getInstance().submit(this::finish));
        rotationTween.tweenMethod(this::setDarken, 0f, 1f, 0.4d).setEaseType(EaseType.EASE_IN_OUT).setTransitionType(TransitionType.SINE);
        rotationTween.start();
        return true;
    }

    public float getyOffset() {
        return yOffset;
    }

    public void setyOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    public float getItemScale() {
        return itemScale;
    }

    public void setItemScale(float itemScale) {
        this.itemScale = itemScale;
    }
}
