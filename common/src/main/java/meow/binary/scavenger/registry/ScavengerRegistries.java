package meow.binary.scavenger.registry;

import meow.binary.scavenger.data.modifier.ScavengerModifier;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScavengerRegistries {
    private static final Map<Identifier, ScavengerModifier> MODIFIERS = new HashMap<>();

    public static ScavengerModifier register(Identifier id, ScavengerModifier modifier) {
        modifier.setId(id);
        MODIFIERS.put(id, modifier);
        return modifier;
    }

    public static ScavengerModifier get(Identifier id) {
        return MODIFIERS.get(id);
    }

    public static Set<Identifier> getIds() {
        return MODIFIERS.keySet();
    }
}
