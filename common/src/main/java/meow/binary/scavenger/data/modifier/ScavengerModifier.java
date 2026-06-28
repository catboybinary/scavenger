package meow.binary.scavenger.data.modifier;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class ScavengerModifier {
    private Identifier id;
    private final Consumer<ServerPlayer> onPlayerTick;
    private final Consumer<ServerLevel> onWorldStart;

    public ScavengerModifier(Consumer<ServerPlayer> onPlayerTick,
                             Consumer<ServerLevel> onWorldStart) {
        this.onPlayerTick = onPlayerTick;
        this.onWorldStart = onWorldStart;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public void playerTick(ServerPlayer player) {
        if (onPlayerTick != null) {
            onPlayerTick.accept(player);
        }
    }

    public void onWorldStart(ServerLevel level) {
        if (onWorldStart != null) {
            onWorldStart.accept(level);
        }
    }

    public boolean hasPlayerTick() {
        return onPlayerTick != null;
    }

    public boolean hasWorldStart() {
        return onWorldStart != null;
    }
}
