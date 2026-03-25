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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import static meow.binary.scavenger.Scavenger.CONFIG;

public final class ScavengerClient {
    public static void init() {
        //System.out.println("test!!");
        ShatterLibNetwork.registerS2CReceiver(SyncScavengerDataPacket.TYPE, SyncScavengerDataPacket.STREAM_CODEC, (syncScavengerDataPacket, packetContext) -> {
            ClientScavengerData.item = syncScavengerDataPacket.getItem();
            ClientScavengerData.modifier = syncScavengerDataPacket.getModifier();

            if (!enforceClientModifiers(packetContext.getPlayer().level())) {
                Minecraft.getInstance().options.sensitivity().set(CONFIG.defaultMouseSensitivity);
                Minecraft.getInstance().options.renderDistance().set(CONFIG.defaultRenderDistance);
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
        if (Modifiers.isActive(Modifiers.TURTLE, level)) {
            Minecraft.getInstance().options.sensitivity().set(0d);
            return true;
        }

        if (Modifiers.isActive(Modifiers.SONIC, level)) {
            Minecraft.getInstance().options.sensitivity().set(1d);
            return true;
        }

        if (Modifiers.isActive(Modifiers.MOLE, level) && Minecraft.getInstance().options.renderDistance().get() != 2) {
            Minecraft.getInstance().options.renderDistance().set(2);
            return true;
        }

        if (Modifiers.isActive(Modifiers.DRUNK, level)) {
            Minecraft.getInstance().options.invertMouseX().set(true);
            Minecraft.getInstance().options.invertMouseY().set(true);
            return true;
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
        double ticks = level.getGameTime()+deltaTracker.getGameTimeDeltaPartialTick(true);

        double totalSeconds = ticks / tickrate;

        int hours = (int)(totalSeconds / 3600);
        int minutes = (int)((totalSeconds % 3600) / 60);
        int seconds = (int)(totalSeconds % 60);
        int millis = (int)((totalSeconds - Math.floor(totalSeconds)) * 100);

        ShatterColor bgColor = new ShatterColor(0, 0, 0, CONFIG.timerBackgroundOpacity);

        String time = String.format("%d:%02d:%02d",
                hours,
                minutes,
                seconds
        );
        String ms = String.format(".%02d", millis);

        int inventoryItemCount = player.getInventory().countItem(ClientScavengerData.item);
        int itemCount = Scavenger.getItemCount(ClientScavengerData.modifier);
        String amountString = inventoryItemCount + " / " + itemCount;

        AnchorPoint anchor = CONFIG.timerAnchorPoint;
        int configX = CONFIG.timerXOffset;
        int configY = CONFIG.timerYOffset;
        int padding = CONFIG.timerSidePadding + 4;

        int noMillisWidth = font.width(time) * 2;
        int millisWidth = font.width(ms);
        int timeWidth = noMillisWidth + millisWidth;
        boolean itemLeft = CONFIG.timerMoveItemLeft;

        //int totalItemWidth = 16 + 4 + font.width(amountString) + 1;

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

        guiGraphics.fill(-4, -4, width+4, height + 4, bgColor.getARGB());

        int timeX = itemLeft ? (width - timeWidth) : 1;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(timeX, 1);
        guiGraphics.pose().scale(2,2);
        guiGraphics.pose().translate(0.5f, 0.5f);
        guiGraphics.drawString(font, time, 0, 0, 0xff444444, false);
        guiGraphics.pose().translate(-0.5f, -0.5f);
        guiGraphics.drawString(font, time, 0, 0, 0xffffffff, false);
        guiGraphics.pose().popMatrix();

        guiGraphics.drawString(font, ms, timeX + noMillisWidth,8, 0xffffffff, true);
        //guiGraphics.vLine(timeX + timeWidth + 2, 0, 14, 0xffffffff);

        int itemX = itemLeft ? 0 : timeX + timeWidth + 5;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(itemX, 0);
        ItemStack stack = new ItemStack(ClientScavengerData.item, itemCount);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.renderItemDecorations(font, stack, 0, 0);
        guiGraphics.pose().popMatrix();

        guiGraphics.vLine(itemLeft ? 18 : itemX - 3, -3, height+2, 0xffffffff);

        RenderUtils.renderOutline(guiGraphics, -3, -3, width + 6, height + 6, 0xffffffff);
        guiGraphics.pose().popMatrix();
    }
}
