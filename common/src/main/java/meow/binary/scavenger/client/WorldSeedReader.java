package meow.binary.scavenger.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;

public final class WorldSeedReader {
    private WorldSeedReader() {
    }

    public static OptionalLong readSeed(Path levelDataPath) throws IOException {
        CompoundTag root = NbtIo.readCompressed(levelDataPath, NbtAccounter.uncompressedQuota());
        CompoundTag data = root.getCompound("Data").orElse(root);
        Optional<Long> seed = data.getCompound("WorldGenSettings").flatMap(tag -> tag.getLong("seed"));

        if (seed.isEmpty()) {
            seed = data.getLong("RandomSeed");
        }

        return seed.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    }
}
