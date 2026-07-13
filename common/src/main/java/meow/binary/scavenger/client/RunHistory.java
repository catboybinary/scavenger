package meow.binary.scavenger.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.shatterbyte.shatterlib.ShatterLib;
import it.hurts.shatterbyte.shatterlib.platform.ShatterLibServices;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.data.ScavengerSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class RunHistory {
    private static final String FILE_NAME = "scavenger_runs.json";
    private static final int CURRENT_VERSION = 1;

    private record RunHistoryFile(int version, List<RunRecord> runs) {
    }

    private static final Codec<RunHistoryFile> FILE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("version").forGetter(RunHistoryFile::version),
                    RunRecord.CODEC.listOf().fieldOf("runs").forGetter(RunHistoryFile::runs)
            ).apply(instance, RunHistoryFile::new));

    public static List<RunRecord> scanAndMerge() {
        List<RunRecord> loaded = load();
        LinkedHashMap<String, RunRecord> byLevelId = new LinkedHashMap<>();

        for (RunRecord record : loaded) {
            if (record.isAuthentic()) {
                byLevelId.put(record.levelId(), record);
            } else {
                ShatterLib.LOGGER.warn("Discarding tampered scavenger run record for level {}", record.levelId());
            }
        }

        scanWorlds(byLevelId);

        List<RunRecord> merged = new ArrayList<>(byLevelId.values());
        if (!merged.equals(loaded)) {
            save(merged);
        }

        return merged;
    }

    private static void scanWorlds(LinkedHashMap<String, RunRecord> byLevelId) {
        LevelStorageSource levelSource = Minecraft.getInstance().getLevelSource();

        List<LevelStorageSource.LevelDirectory> directories;
        try {
            directories = levelSource.findLevelCandidates().levels();
        } catch (Exception exception) {
            ShatterLib.LOGGER.warn("Failed to enumerate worlds for scavenger run history", exception);
            return;
        }

        for (LevelStorageSource.LevelDirectory directory : directories) {
            try {
                scanWorld(directory, byLevelId);
            } catch (Exception exception) {
                ShatterLib.LOGGER.warn("Failed to scan world '{}' for scavenger run history", directory.directoryName(), exception);
            }
        }
    }

    private static void scanWorld(LevelStorageSource.LevelDirectory directory, LinkedHashMap<String, RunRecord> byLevelId) {
        Path dataDir = directory.path().resolve("data");
        Path newDataPath = dataDir.resolve(Scavenger.MOD_ID).resolve("scavenger_data.dat");
        Optional<ScavengerSavedData> data = ScavengerSavedData.readFromDisk(newDataPath);
        if (data.isEmpty()) {
            // World hasn't been migrated to the post-26.2 data layout yet (never loaded on this version).
            Path oldDataPath = dataDir.resolve("scavenger_data.dat");
            data = ScavengerSavedData.readFromDisk(oldDataPath);
        }

        if (data.isEmpty() || !data.get().hasWon()) {
            return;
        }

        OptionalLong seed;
        try {
            seed = WorldSeedReader.readSeed(directory.resourcePath(LevelResource.LEVEL_DATA_FILE));
        } catch (IOException exception) {
            seed = OptionalLong.empty();
        }

        ScavengerSavedData savedData = data.get();
        String levelId = directory.directoryName();
        Optional<Long> boxedSeed = seed.isPresent() ? Optional.of(seed.getAsLong()) : Optional.empty();

        byLevelId.put(levelId, RunRecord.create(levelId, savedData.getItemId(), savedData.getModifierId(), savedData.getWinTimestamp(), boxedSeed));
    }

    private static Path filePath() {
        return ShatterLibServices.platform().getConfigDirectory().resolve(FILE_NAME);
    }

    private static List<RunRecord> load() {
        Path path = filePath();
        if (!Files.isRegularFile(path)) {
            return List.of();
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement json = JsonParser.parseReader(reader);
            RunHistoryFile file = FILE_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

            if (file.version() != CURRENT_VERSION) {
                throw new IllegalStateException("Unsupported scavenger_runs.json version" + file.version());
            }

            return file.runs();
        } catch (Exception exception) {
            ShatterLib.LOGGER.warn("Failed to load scavenger run history, resetting", exception);
            backupCorruptFile(path);
            return List.of();
        }
    }

    private static void backupCorruptFile(Path path) {
        try {
            Files.copy(path, path.resolveSibling(FILE_NAME + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            ShatterLib.LOGGER.warn("Failed to back up corrupted scavenger run history", exception);
        }
    }

    private static void save(List<RunRecord> records) {
        Path path = filePath();

        try {
            JsonElement json = FILE_CODEC.encodeStart(JsonOps.INSTANCE, new RunHistoryFile(CURRENT_VERSION, records)).getOrThrow();
            Files.createDirectories(path.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception exception) {
            ShatterLib.LOGGER.warn("Failed to save scavenger run history", exception);
        }
    }
}
