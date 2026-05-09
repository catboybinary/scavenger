package meow.binary.scavenger.client.particle;

import it.hurts.shatterbyte.shatterlib.client.particle.ExtendedUIParticle;
import it.hurts.shatterbyte.shatterlib.client.particle.UIParticle;
import meow.binary.scavenger.Scavenger;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;

public class StarUIParticle extends ExtendedUIParticle {
    private Vector2f baseSize;

    public StarUIParticle(float maxSpeed, int lifetime, float xStart, float yStart, Layer layer, float zOffset) {
        super(new Texture2D(Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/particle/star.png"), 11, 11),
                maxSpeed, lifetime, xStart, yStart, layer, zOffset);
        this.setGravity(0f);
    }

    @Override
    public void tick() {
        if (this.baseSize == null) {
            this.baseSize = new Vector2f(this.getTransform().getSize());
        }

        super.tick();

        float t = this.getTimeRatio(0f);
        float shrink = Math.max(0f, 1f - t);
        shrink *= shrink;
        this.getTransform().setSize(new Vector2f(this.baseSize).mul(shrink));
    }
}
