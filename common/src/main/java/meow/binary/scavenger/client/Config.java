package meow.binary.scavenger.client;

import it.hurts.shatterbyte.shatterlib.module.config.annotation.Prop;
import it.hurts.shatterbyte.shatterlib.module.config.impl.ShatterConfig;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;

public class Config implements ShatterConfig {
    @Prop
    public float timerBackgroundOpacity = 0.6f;

    @Prop(comment = "Anchor point for the timer. Possible values: TOP_LEFT," +
            " TOP_CENTER," +
            " TOP_RIGHT," +
            " CENTER_LEFT," +
            " CENTER," +
            " CENTER_RIGHT," +
            " BOTTOM_LEFT," +
            " BOTTOM_CENTER," +
            " BOTTOM_RIGHT")
    public AnchorPoint timerAnchorPoint = AnchorPoint.TOP_CENTER;
    @Prop
    public int timerXOffset = 0;
    @Prop
    public int timerYOffset = 0;
    @Prop
    public boolean timerShowMs = true;
    @Prop
    public boolean timerMoveItemLeft = false;
    @Prop
    public int timerSidePadding = 4;
    @Prop(comment = "ARGB color used for the victory accent and winning timer text. Format: #AARRGGBB")
    public String victoryAccentColor = "#FF11D0F0";
    @Prop(comment = "ARGB color used for the regular HUD timer text. Format: #AARRGGBB")
    public String timerDefaultColor = "#FF0DDD48";
    @Prop
    public boolean timerOutlineColorMatch = false;

    @Prop(comment = "Item ids used to limit random item rolls")
    public ArrayList<String> rollableItems = new ArrayList<>() {{
        add("minecraft:dragon_egg");
    }};

    @Prop(comment = "If true, rollableItems is a blacklist. If false, rollableItems is a whitelist")
    public boolean rollableItemsIsBlacklist = true;

    @Prop
    public ArrayList<String> modifierBlacklist = new ArrayList<>() {{
        add("scavenger:none");
        add("scavenger:bedrock");
    }};

    @Prop(comment = "Removes items from the pool after winning by adding or removing them from the rollableItems list")
    public boolean removeItemAfterWin = true;

    @Prop
    public float itemRollTime = 4.5f;

    @Prop
    public float modifierRollTime = 1.45f;

    @Prop
    public float scaleItemWheel = 1f;
    @Prop
    public float scaleModifierWheel = 1f;

    @Prop(comment = "Moves the buttons ")
    public int menuButtonsXOffset = 0;

    public int getVictoryAccentColorArgb() {
        return parseHexColor(victoryAccentColor, 0xff11d0f0);
    }

    public int getTimerDefaultColorArgb() {
        return parseHexColor(timerDefaultColor, 0xffA7C95C);
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
}
