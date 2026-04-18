package meow.binary.scavenger.client;

import dev.architectury.event.events.common.TickEvent;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import it.hurts.shatterbyte.shatterlib.util.RenderUtils;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static meow.binary.scavenger.Scavenger.CONFIG;

public final class ScavengerClient {
    public static void init() {
        //System.out.println("test!!");
        ShatterLibNetwork.registerS2CReceiver(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC, (syncScavengerDataPacket, packetContext) -> {
            ClientScavengerData.item = syncScavengerDataPacket.getItem();
            ClientScavengerData.modifier = syncScavengerDataPacket.getModifier();
            ClientScavengerData.winTimestamp = syncScavengerDataPacket.getWinTimestamp();

            if (!syncScavengerDataPacket.isWin || !CONFIG.removeItemAfterWin) {
                enforceClientModifiers(packetContext.getPlayer().level());
                return;
            }

            String itemId = ClientScavengerData.item.arch$registryName().toString();
            if (CONFIG.rollableItemsIsBlacklist && !CONFIG.rollableItems.contains(itemId)) {
                CONFIG.rollableItems.add(itemId);
                Scavenger.saveConfig();
                return;
            }

            if (!CONFIG.rollableItemsIsBlacklist) {
                CONFIG.rollableItems.remove(itemId);
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
            return false;
        }

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

        float tickrate = level.tickRateManager().tickrate();
        double ticks = level.getGameTime();
        double totalSeconds = ticks / tickrate;

        ShatterColor bgColor = new ShatterColor(0, 0, 0, CONFIG.timerBackgroundOpacity);

        int inventoryItemCount = player.getInventory().countItem(ClientScavengerData.item);
        int itemCount = Scavenger.getItemCount(ClientScavengerData.modifier);

        AnchorPoint anchor = CONFIG.timerAnchorPoint;
        int configX = CONFIG.timerXOffset;
        int configY = CONFIG.timerYOffset;
        int padding = CONFIG.timerSidePadding + 4;

        int hours = (int)(totalSeconds / 3600);
        int minutes = (int)((totalSeconds % 3600) / 60);
        int seconds = (int)(totalSeconds % 60);
        int millis = (int)((totalSeconds - Math.floor(totalSeconds)) * 100);

        String time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        String ms = CONFIG.timerShowMs ? String.format(".%02d", millis) : "";

        int noMillisWidth = font.width(time) * 2;
        int millisWidth = font.width(ms);
        int timeWidth = noMillisWidth + millisWidth;

        boolean itemLeft = CONFIG.timerMoveItemLeft;

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
        renderTimerText(guiGraphics, font, totalSeconds, timeX, 1, CONFIG.timerShowMs, ShatterColor.WHITE);
        int itemX = itemLeft ? 0 : timeX + timeWidth + 5;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(itemX, 0);
        ItemStack stack = new ItemStack(ClientScavengerData.item, itemCount);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.renderItemDecorations(font, stack, 0, 0);
        guiGraphics.pose().popMatrix();

        guiGraphics.vLine(itemLeft ? 18 : itemX - 3, -3, height + 2, 0xffffffff);

        RenderUtils.renderOutline(guiGraphics, -3, -3, width + 6, height + 6, 0xffffffff);

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
