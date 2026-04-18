package meow.binary.scavenger.client.screen;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.client.screen.widget.ItemWheel;
import meow.binary.scavenger.client.screen.widget.ModifierWheel;
import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;

public class ScavengerWorldCreateScreen extends Screen {
    private final CreateWorldScreen createWorldScreen;
    private final Minecraft minecraft;
    private final Runnable createWorld;
    private Item chosenItem = Items.AIR;
    private Identifier chosenModifier = Modifiers.NONE.getId();

    private Tween widgetTween = Tween.create();

    private ItemWheel itemWheel;
    private ModifierWheel modifierWheel;

    public final Random random;

    public Button nextWidget;
    public Button createWidget;

    public ScavengerWorldCreateScreen(CreateWorldScreen createWorldScreen, Minecraft minecraft, Runnable createWorld) {
        super(Component.empty());
        this.createWorldScreen = createWorldScreen;
        this.minecraft = minecraft;
        this.createWorld = createWorld;
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

        nextWidget.active = false;
        createWidget.active = false;
    }

    @Override
    protected void init() {
        if (itemWheel != null) {
            itemWheel.setPosition(this.width / 2 - 105, this.height / 2 - 105);
            this.addRenderableWidget(itemWheel);

            nextWidget.setPosition(this.width / 2 - 64, this.height - 28);
            this.addRenderableWidget(nextWidget);
        } else {
            modifierWheel.setPosition(this.width / 2 - 104, this.height / 2 - 72);
            this.addRenderableWidget(modifierWheel);

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

    private void createWorld() {
        Scavenger.TEMP_DATA.item = this.chosenItem;
        Scavenger.TEMP_DATA.modifier = this.chosenModifier;

        if (this.chosenModifier.equals(Modifiers.DEJAVU.getId())) {
            applyLastWorldSeed();
        }

        this.createWorld.run();
    }

    private void applyLastWorldSeed() {
        OptionalLong seed = getLastWorldSeed(this.minecraft);
        if (seed.isPresent()) {
            WorldCreationUiState uiState = this.createWorldScreen.getUiState();
            uiState.setSeed(Long.toString(seed.getAsLong()));
        }
    }

    private static OptionalLong getLastWorldSeed(Minecraft minecraft) {
        LevelStorageSource levelSource = minecraft.getLevelSource();
        try {
            List<LevelSummary> summaries = levelSource.loadLevelSummaries(levelSource.findLevelCandidates()).join();
            Optional<LevelSummary> lastWorld = summaries.stream()
                    .max(Comparator.comparingLong(LevelSummary::getLastPlayed));

            if (lastWorld.isEmpty()) {
                return OptionalLong.empty();
            }

            Path levelDataPath = levelSource.getLevelPath(lastWorld.get().getLevelId()).resolve(LevelResource.LEVEL_DATA_FILE.getId());
            return readSeed(levelDataPath);
        } catch (Exception exception) {
            return OptionalLong.empty();
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
