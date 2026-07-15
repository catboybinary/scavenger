package meow.binary.scavenger.client;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.resources.Identifier;

public final class ScavengerTimeFormat {
    private ScavengerTimeFormat() {
    }

    public static double tickrate(Identifier modifierId) {
        return modifierId.equals(Modifiers.SPEED_UP.getId()) ? 40d : 20d;
    }

    public static double totalSeconds(long ticks, Identifier modifierId) {
        return ticks / tickrate(modifierId);
    }

    public static String format(long ticks, Identifier modifierId) {
        return format(ticks, modifierId, true);
    }

    public static String format(long ticks, Identifier modifierId, boolean showMs) {
        double totalSeconds = totalSeconds(ticks, modifierId);
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        int millis = (int) ((totalSeconds - Math.floor(totalSeconds)) * 100);

        if (!showMs) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d:%02d.%02d", hours, minutes, seconds, millis);
    }
}
