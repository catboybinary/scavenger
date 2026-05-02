package meow.binary.scavenger.client.particle;

import it.hurts.shatterbyte.shatterlib.client.particle.ExtendedUIParticle;
import it.hurts.shatterbyte.shatterlib.client.particle.UIParticle;
import meow.binary.scavenger.Scavenger;
import net.minecraft.resources.Identifier;

public class StarUIParticle extends ExtendedUIParticle {
    public StarUIParticle(float maxSpeed, int lifetime, float xStart, float yStart, Layer layer, float zOffset) {
        super(new Texture2D(Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/particle/star.png"), 11, 11),
                maxSpeed, lifetime, xStart, yStart, layer, zOffset);
        this.setGravity(0f);
    }
}
