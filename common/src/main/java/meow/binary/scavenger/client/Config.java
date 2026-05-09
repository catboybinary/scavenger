package meow.binary.scavenger.client;

import it.hurts.shatterbyte.shatterlib.module.config.annotation.Prop;
import it.hurts.shatterbyte.shatterlib.module.config.impl.ShatterConfig;

import java.util.ArrayList;

public class Config implements ShatterConfig {
    @Prop
    public TimerCategory timer = new TimerCategory();
    @Prop
    public WheelsCategory wheels = new WheelsCategory();
    @Prop
    public GameplayCategory gameplay = new GameplayCategory();
    @Prop
    public MiscCategory misc = new MiscCategory();

    public int getVictoryAccentColorArgb() {
        return parseHexColor(timer.colorFinished, 0xff11d0f0);
    }

    public int getTimerDefaultColorArgb() {
        return parseHexColor(timer.colorDefault, 0xffA7C95C);
    }

    private static int parseHexColor(String value, int fallback) {
        if (value == null) {
            return fallback;
        }

        String normalized = value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        if (normalized.length() == 6) {
            normalized = "FF" + normalized;
        }

        if (normalized.length() != 8) {
            return fallback;
        }

        try {
            return Integer.parseUnsignedInt(normalized, 16);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static class TimerCategory {
        @Prop
        public float backgroundOpacity = 0.6f;

        @Prop(comment = "Anchor point for the timer. Possible values: TOP_LEFT," +
                " TOP_CENTER," +
                " TOP_RIGHT," +
                " CENTER_LEFT," +
                " CENTER," +
                " CENTER_RIGHT," +
                " BOTTOM_LEFT," +
                " BOTTOM_CENTER," +
                " BOTTOM_RIGHT")
        public AnchorPoint anchorPoint = AnchorPoint.TOP_CENTER;
        @Prop
        public int xOffset = 0;
        @Prop
        public int yOffset = 0;
        @Prop
        public boolean showMs = true;
        @Prop
        public boolean moveItemLeft = false;
        @Prop
        public int sidePadding = 4;
        @Prop(comment = "ARGB color used for the victory accent and winning timer text. Format: #AARRGGBB")
        public String colorFinished = "#FF11D0F0";
        @Prop(comment = "ARGB color used for the regular HUD timer text. Format: #AARRGGBB")
        public String colorDefault = "#FF16F464";
        @Prop
        public boolean outlineColorMatch = false;
    }

    public static class WheelsCategory {
        @Prop
        public float itemRollTime = 4.5f;
        @Prop
        public float modifierRollTime = 1.45f;
        @Prop
        public float scaleItemWheel = 1f;
        @Prop
        public float scaleModifierWheel = 1f;
        @Prop(comment = "Removes the item reveal animation at the item wheel screen")
        public boolean removeItemReveal = false;
        @Prop(comment = "Allows spinning the item and modifier wheels by clicking anywhere on the screen instead of only the wheel widget")
        public boolean clickAnywhereToSpin = false;
    }

    public static class GameplayCategory {
        @Prop(comment = "Item ids used to limit random item rolls")
        public ArrayList<String> rollableItems = new ArrayList<>() {{
            add("minecraft:dragon_egg");
        }};

        @Prop(comment = "If true, rollableItems is a blacklist. If false, rollableItems is a whitelist")
        public boolean rollableItemsIsBlacklist = true;

        @Prop
        public ArrayList<String> modifierBlacklist = new ArrayList<>() {{
            add("scavenger:none");
        }};

        @Prop(comment = "Removes items from the pool after winning by adding or removing them from the rollableItems list")
        public boolean removeItemAfterWin = true;

        @Prop(comment = "Skips the modifier wheel and creates the world immediately with no modifier")
        public boolean skipModifierWheel = false;
    }

    public static class MiscCategory {
        @Prop(comment = "Moves the buttons on the world create screen horizontally")
        public int menuButtonsXOffset = 0;
    }
}
