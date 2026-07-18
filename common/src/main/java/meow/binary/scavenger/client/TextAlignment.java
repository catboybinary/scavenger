package meow.binary.scavenger.client;

public enum TextAlignment {
    INHERIT(-1f),
    LEFT(0f),
    CENTER(0.5f),
    RIGHT(1f);

    public final float xFactor;

    TextAlignment(float xFactor) {
        this.xFactor = xFactor;
    }
}
