package meow.binary.scavenger.client.screen;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.EaseType;
import it.hurts.shatterbyte.shatterlib.client.animation.easing.TransitionType;
import meow.binary.scavenger.Modifier;
import meow.binary.scavenger.client.screen.widget.ItemWheel;
import meow.binary.scavenger.client.screen.widget.ModifierWheel;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ScavengerWorldCreateScreen extends Screen {
    private final Runnable createWorld;
    private Item chosenItem = Items.AIR;
    private Modifier chosenModifier;

    private Tween widgetTween = Tween.create();

    private ItemWheel itemWheel;
    private ModifierWheel modifierWheel;

    public Button nextWidget;

    public ScavengerWorldCreateScreen(Runnable createWorld) {
        super(Component.empty());
        this.createWorld = createWorld;

        itemWheel = new ItemWheel(this.width/2-105, this.height/2-105, 210, 210, this);

        nextWidget = Button.builder(Component.translatable("scavenger.nextWidget"), button -> {
                    button.active = false;

                    widgetTween.kill();
                    widgetTween = Tween.create();
                    widgetTween.setTransitionType(TransitionType.CUBIC);
                    widgetTween.tweenMethod(itemWheel::setxOffset, 0f, -this.width - 210f, 1).setEaseType(EaseType.EASE_IN);
                    widgetTween.start();
                })
                .pos(this.width / 2 - 64, this.height - 32)
                .size(128,16)
                .build();

        nextWidget.active = false;
    }

    @Override
    protected void init() {
        itemWheel.setPosition(this.width/2-105, this.height/2-105);
        this.addRenderableWidget(itemWheel);
        nextWidget.setPosition(this.width / 2 - 64, this.height - 32);
        this.addRenderableWidget(nextWidget);
    }

    public void setChosenItem(Item item) {
        this.chosenItem = item;
        nextWidget.active = chosenItem != Items.AIR;
    }

    public void setChosenModifier(Modifier modifier) {
        this.chosenModifier = modifier;
    }

    private void createWorld() {
        this.createWorld.run();
    }
}
