package meow.binary.scavenger.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
    @Accessor("server")
    MinecraftServer scavenger$getServer();
}
