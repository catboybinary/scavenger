package meow.binary.scavenger.client;

import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import it.hurts.shatterbyte.shatterlib.util.RenderUtils;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.screen.VictoryScreen;
import meow.binary.scavenger.mixin.GameRendererAccessor;
import meow.binary.scavenger.mixin.ToastInstanceAccessor;
import meow.binary.scavenger.mixin.ToastManagerAccessor;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static meow.binary.scavenger.Scavenger.CONFIG;

public final class ScavengerClient {
    private static final Identifier NOIR_POST_EFFECT = Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "noir");
    private static final Random TOURIST_RANDOM = new Random();
    private static boolean touristLanguageApplied;
    private static String previousTouristLanguage;
    private static boolean pendingTouristLanguageRestore;

    public static void init() {
        ShatterLibNetwork.registerS2CReceiver(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC, syncScavengerDataPacket -> {
            ClientScavengerData.item = syncScavengerDataPacket.getItem();
            ClientScavengerData.modifier = syncScavengerDataPacket.getModifier();
            ClientScavengerData.winTimestamp = syncScavengerDataPacket.getWinTimestamp();

            if (!syncScavengerDataPacket.isWin) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != null) {
                    enforceClientModifiers(minecraft.level);
                }
                return;
            }

            Minecraft.getInstance().gui.setScreen(new VictoryScreen());

            if (!CONFIG.gameplay.removeItemAfterWin) {
                return;
            }

            if (CONFIG.gameplay.rollableItemsIsBlacklist && !CONFIG.gameplay.rollableItems.contains(ClientScavengerData.item)) {
                CONFIG.gameplay.rollableItems.add(ClientScavengerData.item);
                Scavenger.saveConfig();
                return;
            }

            if (!CONFIG.gameplay.rollableItemsIsBlacklist) {
                CONFIG.gameplay.rollableItems.remove(ClientScavengerData.item);
                Scavenger.saveConfig();
            }
        });
    }

    public static void onClientTick(Level level) {
        if (!level.isClientSide()) return;
        ScavengerClient.enforceClientModifiers(level);
    }

    public static boolean enforceClientModifiers(Level level) {
        if (ClientScavengerData.isEmpty()) {
            restoreTouristLanguage();
            touristLanguageApplied = false;
            clearNoirPostEffect();
            return false;
        }

        enforceNoirPostEffect();
        enforceTouristLanguage();

        if (Modifiers.isActive(Modifiers.MAIN_CHARACTER, level)) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            return true;
        }

        if (Modifiers.isActive(Modifiers.NPC, level)) {
            Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
            return true;
        }

        return false;
    }

    public static void enforceNoirPostEffect() {
        Minecraft minecraft = Minecraft.getInstance();

        if (ClientScavengerData.is(Modifiers.NOIR)) {
            GameRendererAccessor gameRenderer = (GameRendererAccessor) minecraft.gameRenderer;
            if (!NOIR_POST_EFFECT.equals(minecraft.gameRenderer.currentPostEffect()) || !gameRenderer.scavenger$isEffectActive()) {
                gameRenderer.scavenger$setPostEffect(NOIR_POST_EFFECT);
            }

            return;
        }

        clearNoirPostEffect();
    }

    private static void clearNoirPostEffect() {
        Minecraft minecraft = Minecraft.getInstance();
        if (NOIR_POST_EFFECT.equals(minecraft.gameRenderer.currentPostEffect())) {
            minecraft.gameRenderer.clearPostEffect();
        }
    }

    private static void enforceTouristLanguage() {
        if (!ClientScavengerData.is(Modifiers.TOURIST)) {
            touristLanguageApplied = false;
            return;
        }

        if (touristLanguageApplied) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LanguageManager languageManager = minecraft.getLanguageManager();
        if (previousTouristLanguage == null) {
            previousTouristLanguage = languageManager.getSelected();
        }
        List<String> languages = new ArrayList<>(languageManager.getLanguages().keySet());
        if (languages.isEmpty()) {
            return;
        }

        String currentLanguage = languageManager.getSelected();
        if (languages.size() > 1) {
            languages.remove(currentLanguage);
        }

        if (languages.isEmpty()) {
            return;
        }

        String randomLanguage = languages.get(TOURIST_RANDOM.nextInt(languages.size()));
        languageManager.setSelected(randomLanguage);
        minecraft.options.languageCode = randomLanguage;
        minecraft.options.save();
        minecraft.reloadResourcePacks();
        touristLanguageApplied = true;
    }

    private static void restoreTouristLanguage() {
        if (previousTouristLanguage == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LanguageManager languageManager = minecraft.getLanguageManager();
        if (languageManager.getLanguage(previousTouristLanguage) == null) {
            previousTouristLanguage = null;
            return;
        }

        if (!previousTouristLanguage.equals(languageManager.getSelected())) {
            languageManager.setSelected(previousTouristLanguage);
            minecraft.options.languageCode = previousTouristLanguage;
            minecraft.options.save();
            minecraft.reloadResourcePacks();
        }

        previousTouristLanguage = null;
    }

    public static void onClientDisconnect() {
        pendingTouristLanguageRestore = previousTouristLanguage != null;
        touristLanguageApplied = false;
        clearNoirPostEffect();
        ClientScavengerData.clear();
    }

    public static void onTitleScreenShown() {
        if (!pendingTouristLanguageRestore) {
            return;
        }

        pendingTouristLanguageRestore = false;
        restoreTouristLanguage();
    }

    public static void renderHudInfo(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (ClientScavengerData.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) {
            return;
        }

        boolean won = ClientScavengerData.winTimestamp > 0;

        float tickrate = level.tickRateManager().tickrate();
        double ticks = level.getGameTime();
        if (won) {
            ticks = ClientScavengerData.winTimestamp;
        }

        double totalSeconds = ticks / tickrate;

        ShatterColor bgColor = new ShatterColor(0, 0, 0, CONFIG.timer.backgroundOpacity);

        int itemCount = Scavenger.getItemCount(ClientScavengerData.modifier);

        AnchorPoint anchor = CONFIG.timer.anchorPoint;
        int configX = CONFIG.timer.xOffset;
        int configY = CONFIG.timer.yOffset;

        if (anchor.equals(AnchorPoint.TOP_RIGHT) && CONFIG.timer.moveTimerUnderToasts) {
            List<?> toasts = ((ToastManagerAccessor) mc.gui.toastManager()).scavenger$getVisibleToasts();
            if (!toasts.isEmpty()) {
                for (Object obj : toasts) {
                    configY += ((ToastInstanceAccessor) obj).scavenger$getToast().height();
                }
            }
        }

        int padding = CONFIG.timer.sidePadding + 4;

        int hours = (int)(totalSeconds / 3600);
        int minutes = (int)((totalSeconds % 3600) / 60);
        int seconds = (int)(totalSeconds % 60);
        int millis = (int)((totalSeconds - Math.floor(totalSeconds)) * 100);

        String time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        String ms = CONFIG.timer.showMs ? String.format(".%02d", millis) : "";

        int noMillisWidth = font.width(time) * 2;
        int millisWidth = font.width(ms);
        int timeWidth = noMillisWidth + millisWidth;

        boolean itemLeft = CONFIG.timer.moveItemLeft;

        int width = timeWidth + 6 + 16;
        int height = 16;

        int screenW = guiGraphics.guiWidth() - padding * 2;
        int screenH = guiGraphics.guiHeight() - padding * 2;

        float pivotX = screenW * anchor.xFactor;
        float pivotY = screenH * anchor.yFactor;

        float offsetX = -width * anchor.xFactor;
        float offsetY = -height * anchor.yFactor;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(padding, padding);
        guiGraphics.pose().translate(pivotX + offsetX + configX, pivotY + offsetY + configY);

        guiGraphics.fill(-4, -4, width + 4, height + 4, bgColor.getARGB());

        int timeX = itemLeft ? (width - timeWidth) : 1;
        ShatterColor timerColor = won
                ? new ShatterColor(CONFIG.getVictoryAccentColorArgb())
                : new ShatterColor(CONFIG.getTimerDefaultColorArgb());
        renderTimerText(guiGraphics, font, totalSeconds, timeX, 1, CONFIG.timer.showMs, timerColor);
        int itemX = itemLeft ? 0 : timeX + timeWidth + 5;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(itemX, 0);
        ItemStack stack = new ItemStack(ClientScavengerData.item, itemCount);
        guiGraphics.item(stack, 0, 0);
        guiGraphics.itemDecorations(font, stack, 0, 0);
        guiGraphics.pose().popMatrix();

        guiGraphics.verticalLine(itemLeft ? 18 : itemX - 3, -3, height + 2, CONFIG.timer.outlineColorMatch ? timerColor.getARGB() : 0xffffffff);

        RenderUtils.renderOutline(guiGraphics, -3, -3, width + 6, height + 6, CONFIG.timer.outlineColorMatch ? timerColor.getARGB() : 0xffffffff);

        int textY;
        if (anchor.yFactor > 0.5f) {
            textY = -4 - 2;
        } else {
            textY = height + 4 + 2;
        }

        if (CONFIG.timer.showModifierText && !ClientScavengerData.modifier.equals(Modifiers.NONE.getId())) {
            Component modifierText = Component.translatable("scavenger.victory.modifier_label").withStyle(ChatFormatting.WHITE).withColor(0xffdddddd)
                    .append(" ")
                    .append(Modifiers.getName(ClientScavengerData.modifier).withStyle(ChatFormatting.WHITE));
            float alignFactor = CONFIG.timer.textAlignment == TextAlignment.INHERIT
                    ? anchor.xFactor
                    : CONFIG.timer.textAlignment.xFactor;
            int textX;
            if (alignFactor == 0f) {
                textX = -4;
            } else if (alignFactor == 1f) {
                textX = width + 4 - font.width(modifierText);
            } else {
                textX = (width) / 2 - font.width(modifierText) / 2;
            }

            if (anchor.yFactor > 0.5f) {
                textY -= font.lineHeight;
            }

            guiGraphics.text(font, modifierText, textX, textY, 0xFFFFFFFF, true);

            if (!(anchor.yFactor > 0.5f)) {
                textY += font.lineHeight + 1;
            } else {
                textY -= 1;
            }
        }

        if (CONFIG.timer.showItemName) {
            Component itemComponent = Component.translatable("scavenger.victory.item_label").withColor(0xffdddddd)
                    .append(" ")
                    .append(ClientScavengerData.item.getDefaultInstance().getStyledHoverName().copy());
            float alignFactor = CONFIG.timer.textAlignment == TextAlignment.INHERIT
                    ? anchor.xFactor
                    : CONFIG.timer.textAlignment.xFactor;

            List<FormattedCharSequence> lines = font.split(itemComponent, 172);
            boolean reverse = anchor.yFactor > 0.5f;
            for (int i = 0; i < lines.size(); i++) {
                FormattedCharSequence line = lines.get(reverse ? lines.size() - 1 - i : i);
                int textX;
                int lineWidth = font.width(line);
                if (alignFactor == 0f) {
                    textX = -4;
                } else if (alignFactor == 1f) {
                    textX = width + 4 - lineWidth;
                } else {
                    textX = (width) / 2 - lineWidth / 2;
                }

                if (anchor.yFactor > 0.5f) {
                    textY -= font.lineHeight;
                }

                guiGraphics.text(font, line, textX, textY, 0xFFFFFFFF, true);

                if (anchor.yFactor > 0.5f) {
                    textY -= 1;
                } else {
                    textY += font.lineHeight + 1;
                }
            }
        }

        guiGraphics.pose().popMatrix();
    }

    public static void renderTimerText(GuiGraphicsExtractor guiGraphics, Font font, double totalSeconds, int x, int y, boolean showMs, ShatterColor color) {
        int hours = (int)(totalSeconds / 3600);
        int minutes = (int)((totalSeconds % 3600) / 60);
        int seconds = (int)(totalSeconds % 60);
        int millis = (int)((totalSeconds - Math.floor(totalSeconds)) * 100);

        ShatterColor shadow = color.multiply(0.25f, 0.25f, 0.25f, 1f);

        String time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        String ms = showMs ? String.format(".%02d", millis) : "";

        int noMillisWidth = font.width(time) * 2;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(2, 2);
        guiGraphics.pose().translate(0.5f, 0.5f);
        guiGraphics.text(font, time, 0, 0, shadow.getARGB(), false);
        guiGraphics.pose().translate(-0.5f, -0.5f);
        guiGraphics.text(font, time, 0, 0, color.getARGB(), false);
        guiGraphics.pose().popMatrix();

        guiGraphics.text(font, ms, noMillisWidth, 8, color.getARGB(), true);

        guiGraphics.pose().popMatrix();
    }
}
