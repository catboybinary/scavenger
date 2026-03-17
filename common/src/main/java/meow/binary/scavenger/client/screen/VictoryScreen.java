package meow.binary.scavenger.client.screen;

import it.hurts.shatterbyte.shatterlib.client.animation.Tween;
import meow.binary.scavenger.client.ClientScavengerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class VictoryScreen extends Screen {
    Button disconnectButton;
    Tween tween = Tween.create();

    private void setValue(float value) {
        this.value = value;
    }

    float value = 0;

    public VictoryScreen() {
        super(Component.empty());
        tween.tweenMethod(this::setValue, 0f, Mth.PI*2, 4d);
        tween.setLoops(-1);
        tween.start();

        this.disconnectButton = Button.builder(
                        CommonComponents.disconnectButtonLabel(this.minecraft.isLocalServer()),
                        button -> {
                            button.active = false;
                            this.minecraft
                                    .getReportingContext()
                                    .draftReportHandled(this.minecraft, this, () -> this.minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE), true);
                        }
                )
                .width(204)
                .build();
    }

    @Override
    protected void init() {
        this.disconnectButton.setPosition(this.width/2-102, this.height-28);
        this.addRenderableWidget(this.disconnectButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Font font = Minecraft.getInstance().font;
        Component victoryText = Component.translatable("scavenger.victory");

        int cx = this.width/2;
        int cy = this.height/2;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.width/2f, 64 + Mth.cos(value)*2);
        guiGraphics.pose().scale(2);
        guiGraphics.pose().rotate(Mth.sin(value)/8f);
        guiGraphics.drawString(font, victoryText, -font.width(victoryText)/2, -4, 0xffffffff, true);
        guiGraphics.pose().popMatrix();

        guiGraphics.fill(cx-96, cy-32, cx+96, cy+64, 0x66000000);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.width/2f, this.height/2f);
        guiGraphics.pose().scale(2f);
        guiGraphics.renderItem(ClientScavengerData.item.getDefaultInstance(), -8, -8);
        guiGraphics.pose().popMatrix();
    }

    @Override
    public void onClose() {
        super.onClose();
        tween.kill();
    }
}
