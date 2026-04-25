package meow.binary.scavenger.client.screen;

import meow.binary.scavenger.registry.Modifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ManualSelectionScreen extends Screen {
    private final ScavengerWorldCreateScreen parent;
    private final List<Identifier> modifiers = Modifiers.getIds().stream()
            .sorted(Comparator.comparing(identifier -> Modifiers.getName(identifier).getString()))
            .toList();

    private EditBox itemIdBox;
    private CycleButton<Identifier> modifierButton;
    private Button createButton;

    private Item resolvedItem = Items.AIR;
    private Identifier resolvedModifier = Modifiers.NONE.getId();
    private boolean validItem;
    private boolean validModifier;

    public ManualSelectionScreen(ScavengerWorldCreateScreen parent) {
        super(Component.translatable("scavenger.manual_selection"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelWidth = 320;
        int panelHeight = 180;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - panelHeight / 2;

        this.itemIdBox = new EditBox(this.font, panelX + 20, panelY + 34, panelWidth - 40, 20, Component.translatable("scavenger.item_id"));
        this.itemIdBox.setMaxLength(128);
        this.itemIdBox.setHint(Component.literal("minecraft:diamond"));
        this.itemIdBox.setValue(parent.getChosenItem().arch$registryName().toString());
        this.itemIdBox.setResponder(value -> this.updateValidation());
        this.addRenderableWidget(this.itemIdBox);

        this.modifierButton = this.addRenderableWidget(
                CycleButton.builder(Modifiers::getName, parent.getChosenModifier())
                        .withValues(this.modifiers)
                        .displayOnlyValue()
                        .create(panelX + 20, panelY + 88, panelWidth - 40, 20, Component.translatable("scavenger.modifier_id"), (button, value) -> {
                            this.resolvedModifier = value;
                            this.updateValidation();
                        })
        );
        this.modifierButton.setValue(parent.getChosenModifier());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                .bounds(panelX + 20, panelY + panelHeight - 40, 128, 20)
                .build());

        this.createButton = this.addRenderableWidget(Button.builder(Component.translatable("scavenger.create"), button ->
                        this.parent.createWorld(this.resolvedItem, this.resolvedModifier))
                .bounds(panelX + panelWidth - 148, panelY + panelHeight - 40, 128, 20)
                .build());

        this.setInitialFocus(this.itemIdBox);
        this.updateValidation();
    }

    private void updateValidation() {
        String itemValue = this.itemIdBox.getValue().trim();

        this.validItem = false;
        this.resolvedItem = Items.AIR;
        this.validModifier = this.modifierButton != null;
        if (this.modifierButton != null) {
            this.resolvedModifier = this.modifierButton.getValue();
        } else {
            this.resolvedModifier = Modifiers.NONE.getId();
        }

        Identifier itemId = Identifier.tryParse(itemValue);
        if (itemId != null) {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
            if (item.isPresent() && item.get() != Items.AIR) {
                this.validItem = true;
                this.resolvedItem = item.get();
            }
        }

        if (this.createButton != null) {
            this.createButton.active = this.validItem && this.validModifier;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int panelWidth = 320;
        int panelHeight = 180;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - panelHeight / 2;

        guiGraphics.fill(panelX - 4, panelY - 4, panelX + panelWidth + 4, panelY + panelHeight + 4, 0xaa000000);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xee1a222b);
        guiGraphics.renderOutline(panelX, panelY, panelWidth, panelHeight, 0xffffffff);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 12, 0xffffffff);

        guiGraphics.drawString(this.font, Component.translatable("scavenger.item_id"), panelX + 20, panelY + 22, 0xff9fdce7, false);
        guiGraphics.drawString(this.font, Component.translatable("scavenger.modifier_id"), panelX + 20, panelY + 76, 0xff9fdce7, false);

        Component itemStatus = this.validItem
                ? this.resolvedItem.getName().copy().withStyle(ChatFormatting.GREEN)
                : Component.translatable("scavenger.invalid_item").withStyle(ChatFormatting.RED);
        Component modifierStatus = this.validModifier
                ? Modifiers.getName(this.resolvedModifier).withStyle(ChatFormatting.GREEN)
                : Component.translatable("scavenger.invalid_modifier").withStyle(ChatFormatting.RED);

        guiGraphics.drawString(this.font, itemStatus, panelX + 20, panelY + 56, 0xffffffff, false);
        guiGraphics.drawString(this.font, modifierStatus, panelX + 20, panelY + 110, 0xffffffff, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.validItem) {
            guiGraphics.renderItem(this.resolvedItem.getDefaultInstance(), panelX + panelWidth - 38, panelY + 36);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 257 || event.key() == 335) {
            if (this.createButton.active) {
                this.parent.createWorld(this.resolvedItem, this.resolvedModifier);
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
