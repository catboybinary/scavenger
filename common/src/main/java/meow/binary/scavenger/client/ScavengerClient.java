package meow.binary.scavenger.client;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Mod;
import it.hurts.shatterbyte.shatterlib.module.network.ShatterLibNetwork;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class ScavengerClient {
    public static void init() {
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

        Font font = Minecraft.getInstance().font;
        Player player = Minecraft.getInstance().player;
        Level level = Minecraft.getInstance().level;
        float tickrate = level.tickRateManager().tickrate();
        long ticks = level.getGameTime();
        String time = LocalTime.ofSecondOfDay((long) Math.floor(ticks / tickrate))
                .format(DateTimeFormatter.ISO_TIME);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(guiGraphics.guiWidth()/2f, 0);
        guiGraphics.drawString(font, time, -font.width(time)/2, 8, 0xffffffff, true);

        int inventoryItemCount = player.getInventory().countItem(ClientScavengerData.item);
        int itemCount = Scavenger.getItemCount(ClientScavengerData.modifier);

        String amountString = inventoryItemCount + " / " + itemCount;
        int totalItemWidth = 16 + 4 + font.width(amountString);

        guiGraphics.renderItem(ClientScavengerData.item.getDefaultInstance(), -totalItemWidth/2, 20);
        guiGraphics.drawString(font, amountString, -totalItemWidth/2 + 16 + 4, 20 + 4, inventoryItemCount >= itemCount ? 0xff00ff00 : 0xffffffff, true);
        guiGraphics.pose().popMatrix();
    }
}
