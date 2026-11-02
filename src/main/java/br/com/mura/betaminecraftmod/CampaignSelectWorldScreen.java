package br.com.mura.betaminecraftmod;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

final class CampaignSelectWorldScreen extends SelectWorldScreen {
    CampaignSelectWorldScreen(Screen parent) { super(parent); }
    @Override protected void init() {
        super.init();
        var buttons = children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
        for (Button button : buttons) {
            if (!(button.getMessage().getContents() instanceof TranslatableContents text)) continue;
            if (text.getKey().equals("selectWorld.create")) {
                removeWidget(button);
                addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), b -> minecraft.gui.setScreen(new JourneyCreationScreen(this)))
                    .bounds(button.getX(), button.getY(), button.getWidth(), button.getHeight()).build());
            }
        }
    }
}
