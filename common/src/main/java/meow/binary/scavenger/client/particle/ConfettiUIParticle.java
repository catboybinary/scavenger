package meow.binary.scavenger.client.particle;

import it.hurts.shatterbyte.shatterlib.client.particle.ExtendedUIParticle;
import it.hurts.shatterbyte.shatterlib.client.particle.UIParticle;
import meow.binary.scavenger.Scavenger;
import net.minecraft.resources.Identifier;

public class ConfettiUIParticle extends ExtendedUIParticle {
    public ConfettiUIParticle(float maxSpeed, int lifetime, float xStart, float yStart, Layer layer, float zOffset) {
        super(new Texture2D(Identifier.fromNamespaceAndPath(Scavenger.MOD_ID, "textures/particle/confetti.png"), 3, 3),
                maxSpeed, lifetime, xStart, yStart, layer, zOffset);
        this.setGravity(0.5f);
    }
}
