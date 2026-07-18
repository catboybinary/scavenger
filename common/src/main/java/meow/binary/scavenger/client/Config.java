package meow.binary.scavenger.client;

import it.hurts.shatterbyte.shatterlib.module.config.ShatterConfig;
import it.hurts.shatterbyte.shatterlib.module.config.type.annotation.Comment;
import it.hurts.shatterbyte.shatterlib.util.ShatterColor;
import net.minecraft.world.item.Item;

import java.util.ArrayList;

public class Config extends ShatterConfig {
    public TimerCategory timer = new TimerCategory();
    public WheelsCategory wheels = new WheelsCategory();
    public GameplayCategory gameplay = new GameplayCategory();
    public MiscCategory misc = new MiscCategory();

    public int getVictoryAccentColorArgb() {
        return timer.colorFinished.getARGB();
    }

    public int getTimerDefaultColorArgb() {
        return timer.colorDefault.getARGB();
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

    @Override
    public String getName() {
        return "scavenger";
    }

    @Override
    public int getSchemaVersion() {
        return 0;
    }

    public static class TimerCategory {
        public float backgroundOpacity = 0.6f;

        @Comment("Anchor point for the timer. Possible values: TOP_LEFT," +
                " TOP_CENTER," +
                " TOP_RIGHT," +
                " CENTER_LEFT," +
                " CENTER," +
                " CENTER_RIGHT," +
                " BOTTOM_LEFT," +
                " BOTTOM_CENTER," +
                " BOTTOM_RIGHT")
        public AnchorPoint anchorPoint = AnchorPoint.TOP_CENTER;
        public int xOffset = 0;
        public int yOffset = 0;
        @Comment("Moves the timer out of the way whenever toasts are on screen. (Only works if the anchor point is TOP_RIGHT)")
        public boolean moveTimerUnderToasts = true;
        public boolean showMs = true;
        public boolean moveItemLeft = false;
        public int sidePadding = 4;
        @Comment("Color used for the victory accent and winning timer text. Format: #AARRGGBB")
        public ShatterColor colorFinished = new ShatterColor(0xFF11D0F0);
        @Comment("Color used for the regular HUD timer text. Format: #AARRGGBB")
        public ShatterColor colorDefault = new ShatterColor(0xFF16F464);
        public boolean outlineColorMatch = false;
        public boolean showModifierText = true;
        public boolean showItemName = true;
        @Comment("Text alignment for the modifier and item name below the timer. INHERIT (follows anchor point), LEFT, CENTER, or RIGHT")
        public TextAlignment textAlignment = TextAlignment.INHERIT;
    }

    public static class WheelsCategory {
        public float itemRollTime = 4.5f;
        public float modifierRollTime = 1.45f;
        public float scaleItemWheel = 1f;
        public float scaleModifierWheel = 1f;
        @Comment("Removes the item reveal animation at the item wheel screen")
        public boolean removeItemReveal = false;
        @Comment("Allows spinning the item and modifier wheels by clicking anywhere on the screen instead of only the wheel widget")
        public boolean clickAnywhereToSpin = false;
    }

    public static class GameplayCategory {
        @Comment("Item ids used to limit random item rolls")
        public ArrayList<Item> rollableItems = new ArrayList<>();

        @Comment("If true, rollableItems is a blacklist. If false, rollableItems is a whitelist")
        public boolean rollableItemsIsBlacklist = true;

        public ArrayList<String> modifierBlacklist = new ArrayList<>() {{
            add("scavenger:none");
        }};

        @Comment("Removes items from the pool after winning by adding or removing them from the rollableItems list")
        public boolean removeItemAfterWin = true;

        @Comment("Skips the modifier wheel and creates the world immediately with no modifier")
        public boolean skipModifierWheel = false;
    }

    public static class MiscCategory {
        @Comment("Makes the Victory! title static instead of bobbing")
        public boolean staticVictoryTitle = false;
        @Comment("Moves the buttons on the world create screen horizontally")
        public int menuButtonsXOffset = 0;
    }
}
