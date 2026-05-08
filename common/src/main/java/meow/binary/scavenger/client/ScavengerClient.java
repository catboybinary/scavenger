package meow.binary.scavenger.client;

import dev.architectury.event.events.common.TickEvent;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import it.hurts.shatterbyte.shatterlib.util.RenderUtils;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.screen.VictoryScreen;
import meow.binary.scavenger.mixin.GameRendererAccessor;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.resources.Identifier;
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
        //System.out.println("test!!");
        ShatterLibNetwork.registerS2CReceiver(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC, (syncScavengerDataPacket, packetContext) -> {
            ClientScavengerData.item = syncScavengerDataPacket.getItem();
            ClientScavengerData.modifier = syncScavengerDataPacket.getModifier();
            ClientScavengerData.winTimestamp = syncScavengerDataPacket.getWinTimestamp();

            if (!syncScavengerDataPacket.isWin) {
                enforceClientModifiers(packetContext.getPlayer().level());
                return;
            }

            Minecraft.getInstance().setScreen(new VictoryScreen());

            if (!CONFIG.gameplay.removeItemAfterWin) {
                return;
            }

            String itemId = ClientScavengerData.item.arch$registryName().toString();
            if (CONFIG.gameplay.rollableItemsIsBlacklist && !CONFIG.gameplay.rollableItems.contains(itemId)) {
                CONFIG.gameplay.rollableItems.add(itemId);
                Scavenger.saveConfig();
                return;
            }

            if (!CONFIG.gameplay.rollableItemsIsBlacklist) {
                CONFIG.gameplay.rollableItems.remove(itemId);
                Scavenger.saveConfig();
            }
        });

        TickEvent.PLAYER_POST.register(player -> {
            Level level = player.level();
            if (!level.isClientSide() || player.tickCount % 20 != 0) {
                return;
            }

            ScavengerClient.enforceClientModifiers(level);
        });
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

    public static void renderHudInfo(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
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

        int inventoryItemCount = player.getInventory().countItem(ClientScavengerData.item);
        int itemCount = Scavenger.getItemCount(ClientScavengerData.modifier);

        AnchorPoint anchor = CONFIG.timer.anchorPoint;
        int configX = CONFIG.timer.xOffset;
        int configY = CONFIG.timer.yOffset;
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
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.renderItemDecorations(font, stack, 0, 0);
        guiGraphics.pose().popMatrix();

        guiGraphics.vLine(itemLeft ? 18 : itemX - 3, -3, height + 2, CONFIG.timer.outlineColorMatch ? timerColor.getARGB() : 0xffffffff);

        RenderUtils.renderOutline(guiGraphics, -3, -3, width + 6, height + 6, CONFIG.timer.outlineColorMatch ? timerColor.getARGB() : 0xffffffff);

        guiGraphics.pose().popMatrix();
    }

    public static void renderTimerText(GuiGraphics guiGraphics, Font font, double totalSeconds, int x, int y, boolean showMs, ShatterColor color) {
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

        // main big time
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(2, 2);
        guiGraphics.pose().translate(0.5f, 0.5f);
        guiGraphics.drawString(font, time, 0, 0, shadow.getARGB(), false);
        guiGraphics.pose().translate(-0.5f, -0.5f);
        guiGraphics.drawString(font, time, 0, 0, color.getARGB(), false);
        guiGraphics.pose().popMatrix();

        // milliseconds
        guiGraphics.drawString(font, ms, noMillisWidth, 8, color.getARGB(), true);

        guiGraphics.pose().popMatrix();
    }
}
