package meow.binary.scavenger.client;

import dev.architectury.event.events.common.TickEvent;
import it.hurts.shatterbyte.shatterlib.module.config.ConfigManager;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ScavengerClient {
    public static final Config CONFIG = new Config();

    public static void init() {
        ConfigManager.registerConfig("scavenger", CONFIG);

        //System.out.println("test!!");
        ShatterLibNetwork.registerS2CReceiver(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC, (syncScavengerDataPacket, packetContext) -> {
            ClientScavengerData.item = syncScavengerDataPacket.getItem();
            ClientScavengerData.modifier = syncScavengerDataPacket.getModifier();
        });

        TickEvent.PLAYER_POST.register(player -> {
            Level level = player.level();
            if (!level.isClientSide()) {
                return;
            }

            if (Modifiers.isActive(Modifiers.TURTLE, level)) {
                Minecraft.getInstance().options.sensitivity().set(0d);
            } else if (Modifiers.isActive(Modifiers.SONIC, level)) {
                Minecraft.getInstance().options.sensitivity().set(1d);
            } else {
                Minecraft.getInstance().options.sensitivity().set(0.5d);
            }
        });
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
        long ticks = level.getGameTime();
        String time = LocalTime.ofSecondOfDay((long) Math.floor(ticks / tickrate))
                .format(DateTimeFormatter.ISO_TIME);

        int inventoryItemCount = player.getInventory().countItem(ClientScavengerData.item);
        int itemCount = Scavenger.getItemCount(ClientScavengerData.modifier);
        String amountString = inventoryItemCount + " / " + itemCount;

        AnchorPoint anchor = ScavengerClient.CONFIG.timerAnchorPoint;
        int configX = ScavengerClient.CONFIG.timerXOffset;
        int configY = ScavengerClient.CONFIG.timerYOffset;

        int timeWidth = font.width(time);
        int totalItemWidth = 16 + 4 + font.width(amountString);

        int width = Math.max(timeWidth, totalItemWidth);
        int height = 36;

        int screenW = guiGraphics.guiWidth() - 16;
        int screenH = guiGraphics.guiHeight() - 16;

        float pivotX = screenW * anchor.xFactor;
        float pivotY = screenH * anchor.yFactor;

        float offsetX = -width * anchor.xFactor;
        float offsetY = -height * anchor.yFactor;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(8, 8);

        guiGraphics.pose().translate(pivotX + offsetX + configX, pivotY + offsetY + configY);

        int timeX = (int) ((width - timeWidth*2) * anchor.xFactor);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(timeX, 0);
        guiGraphics.pose().scale(2,2);
        guiGraphics.drawString(font, time, 0, 0, 0xffffffff, true);
        guiGraphics.pose().popMatrix();

        int rowX = (width - totalItemWidth) / 2;
        //guiGraphics.fill(rowX, 12, rowX+16, 12+16, 0x33ff0000);
        guiGraphics.renderItem(ClientScavengerData.item.getDefaultInstance(), rowX, 20);
        guiGraphics.drawString(
                font,
                amountString,
                rowX + 16 + 4,
                20 + 4,
                inventoryItemCount >= itemCount ? 0xff00ff00 : 0xffffffff,
                true
        );

        guiGraphics.pose().popMatrix();
    }
}
