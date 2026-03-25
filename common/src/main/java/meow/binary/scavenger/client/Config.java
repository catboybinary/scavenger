package meow.binary.scavenger.client;

import it.hurts.shatterbyte.shatterlib.module.config.annotation.Prop;
import it.hurts.shatterbyte.shatterlib.module.config.impl.ShatterConfig;

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
    public boolean timerMoveItemLeft = false;
    @Prop
    public int timerSidePadding = 4;

    @Prop
    public int defaultRenderDistance = 12;
    @Prop
    public double defaultMouseSensitivity = 0.5f;
}
