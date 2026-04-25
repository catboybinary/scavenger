package meow.binary.scavenger.client.screen;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.screen.widget.ItemWheel;
import meow.binary.scavenger.client.screen.widget.ModifierWheel;
import meow.binary.scavenger.mixin.CreateWorldScreenAccessor;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;

public class ScavengerWorldCreateScreen extends Screen {
    private static boolean creatingVanillaWorld;

    private final CreateWorldScreen createWorldScreen;
    private final Minecraft minecraft;
    private Item chosenItem = Items.AIR;
    private Identifier chosenModifier = Modifiers.NONE.getId();

    private Tween widgetTween = Tween.create();

    private ItemWheel itemWheel;
    private ModifierWheel modifierWheel;

    public final Random random;

    public Button nextWidget;
    public Button createWidget;
    public Button manualWidget;

    public ScavengerWorldCreateScreen(CreateWorldScreen createWorldScreen, Minecraft minecraft) {
        super(Component.empty());
        this.createWorldScreen = createWorldScreen;
        this.minecraft = minecraft;
        this.random = new Random(createWorldScreen.getUiState().getSettings().options().seed());

        itemWheel = new ItemWheel(this.width/2-105, this.height/2-105, 210, 210, this);
        modifierWheel = new ModifierWheel(this.width/2-104, this.height/2-72, 208, 144, this);

        nextWidget = Button.builder(Component.translatable("scavenger.next_widget"), button -> {
                    button.active = false;

                    widgetTween.kill();
                    widgetTween = Tween.create();
                    widgetTween.setTransitionType(TransitionType.CUBIC);
                    widgetTween.tweenMethod(itemWheel::setxOffset, 0f, -this.width - 210f, 0.66).setEaseType(EaseType.EASE_IN);
                    widgetTween.tweenRunnable(() -> {
                        this.removeWidget(itemWheel);
                        itemWheel = null;

                        modifierWheel.setxOffset(this.width/2+104);
                        this.rebuildWidgets();
                    });
                    widgetTween.tweenMethod(modifierWheel::setxOffset, this.width/2+104f, 0f, 0.66).setEaseType(EaseType.EASE_OUT);
                    widgetTween.start();
                })
                .size(128,20)
                .build();

        createWidget = Button.builder(Component.translatable("scavenger.create"), button -> this.createWorld())
                .size(128,20)
                .build();

        manualWidget = Button.builder(Component.translatable("scavenger.manual_selection"), button ->
                        this.minecraft.setScreen(new ManualSelectionScreen(this))
                )
                .size(128, 20)
                .build();

        nextWidget.active = false;
        createWidget.active = false;
    }

    @Override
    protected void init() {
        if (itemWheel != null) {
            itemWheel.setPosition(this.width / 2 - 105, this.height / 2 - 105);
            this.addRenderableWidget(itemWheel);

            manualWidget.setPosition(this.width / 2 - 64, this.height - 52);
            this.addRenderableWidget(manualWidget);
            nextWidget.setPosition(this.width / 2 - 64, this.height - 28);
            this.addRenderableWidget(nextWidget);
        } else {
            modifierWheel.setPosition(this.width / 2 - 104, this.height / 2 - 72);
            this.addRenderableWidget(modifierWheel);

            manualWidget.setPosition(this.width / 2 - 64, this.height - 52);
            this.addRenderableWidget(manualWidget);
            createWidget.setPosition(this.width / 2 - 64, this.height - 28);
            this.addRenderableWidget(createWidget);
        }
    }

    public void setChosenItem(Item item) {
        this.chosenItem = item;
        nextWidget.active = !chosenItem.equals(Items.AIR);
    }

    public void setChosenModifier(Identifier modifier) {
        this.chosenModifier = modifier;
        createWidget.active = true;
    }

    public Item getChosenItem() {
        return this.chosenItem;
    }

    public Identifier getChosenModifier() {
        return this.chosenModifier;
    }

    public void createWorld() {
        this.createWorld(this.chosenItem, this.chosenModifier);
    }

    public void createWorld(Item item, Identifier modifier) {
        this.chosenItem = item;
        this.chosenModifier = modifier;

        Scavenger.TEMP_DATA.item = item;
        Scavenger.TEMP_DATA.modifier = modifier;

        if (modifier.equals(Modifiers.DEJAVU.getId())) {
            applyLastWorldSeed();
        } else if (modifier.equals(Modifiers.LARGE_BIOMES.getId())) {
            applyWorldPreset(WorldPresets.LARGE_BIOMES);
        } else if (modifier.equals(Modifiers.AMPLIFIED.getId())) {
            applyWorldPreset(WorldPresets.AMPLIFIED);
        }

        creatingVanillaWorld = true;
        try {
            ((CreateWorldScreenAccessor) this.createWorldScreen).scavenger$onCreate();
        } finally {
            creatingVanillaWorld = false;
        }
    }

    public static boolean isCreatingVanillaWorld() {
        return creatingVanillaWorld;
    }

    private void applyLastWorldSeed() {
        OptionalLong seed = getLastWorldSeed(this.minecraft);
        if (seed.isPresent()) {
            WorldCreationUiState uiState = this.createWorldScreen.getUiState();
            uiState.setSeed(Long.toString(seed.getAsLong()));
        }
    }

    private void applyWorldPreset(ResourceKey<WorldPreset> presetKey) {
        WorldCreationUiState uiState = this.createWorldScreen.getUiState();
        HolderLookup.RegistryLookup<WorldPreset> presets = uiState.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET);
        Optional<Holder.Reference<WorldPreset>> preset = presets.get(presetKey);
        preset.ifPresent(holder -> uiState.setWorldType(new WorldCreationUiState.WorldTypeEntry(holder)));
    }

    private static OptionalLong getLastWorldSeed(Minecraft minecraft) {
        LevelStorageSource levelSource = minecraft.getLevelSource();
        try {
            List<LevelSummary> summaries = levelSource.loadLevelSummaries(levelSource.findLevelCandidates()).join();
            Optional<LevelSummary> lastWorld = summaries.stream()
                    .max(Comparator.comparingLong(LevelSummary::getLastPlayed));

            if (lastWorld.isEmpty()) {
                return getLastWorldSeedFromSaveFolder(levelSource.getBaseDir());
            }

            Path levelDataPath = levelSource.getLevelPath(lastWorld.get().getLevelId()).resolve(LevelResource.LEVEL_DATA_FILE.getId());
            OptionalLong seed = readSeed(levelDataPath);
            if (seed.isPresent()) {
                return seed;
            }
        } catch (Exception ignored) {
            return getLastWorldSeedFromSaveFolder(levelSource.getBaseDir());
        }

        return getLastWorldSeedFromSaveFolder(levelSource.getBaseDir());
    }

    private static OptionalLong getLastWorldSeedFromSaveFolder(Path savesFolder) {
        try (var paths = Files.list(savesFolder)) {
            Optional<Path> latestLevelData = paths
                    .map(path -> path.resolve(LevelResource.LEVEL_DATA_FILE.getId()))
                    .filter(Files::isRegularFile)
                    .max(Comparator.comparingLong(ScavengerWorldCreateScreen::getLastModifiedTime));

            if (latestLevelData.isEmpty()) {
                return OptionalLong.empty();
            }

            return readSeed(latestLevelData.get());
        } catch (Exception ignored) {
            return OptionalLong.empty();
        }
    }

    private static long getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException exception) {
            return Long.MIN_VALUE;
        }
    }

    private static OptionalLong readSeed(Path levelDataPath) throws IOException {
        CompoundTag root = NbtIo.readCompressed(levelDataPath, NbtAccounter.uncompressedQuota());
        CompoundTag data = root.getCompound("Data").orElse(root);
        Optional<Long> seed = data.getCompound("WorldGenSettings").flatMap(tag -> tag.getLong("seed"));

        if (seed.isEmpty()) {
            seed = data.getLong("RandomSeed");
        }

        return seed.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    }
}
