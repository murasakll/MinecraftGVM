package br.com.mura.betaminecraftmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;

final class JourneyCreationScreen extends Screen {
    private final Screen parent;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean hardcore;
    JourneyCreationScreen(Screen parent) { super(Component.literal("Go back in the days")); this.parent = parent; }
    @Override protected void init() {
        JourneyWorld.loading = false;
        int x = width / 2 - 110, y = height / 2 - 28;
        Button difficultyButton = addRenderableWidget(Button.builder(difficultyLabel(), b -> {
            difficulty = Difficulty.byId((difficulty.getId() + 1) % 4);
            b.setMessage(difficultyLabel());
        }).bounds(x, y, 220, 20).build());
        difficultyButton.active = !hardcore;
        addRenderableWidget(Button.builder(modeLabel(), b -> {
            hardcore = !hardcore;
            b.setMessage(modeLabel());
            difficultyButton.active = !hardcore;
            difficultyButton.setMessage(difficultyLabel());
        }).bounds(x, y + 26, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Criar novo mundo"), b -> {
            b.active = false;
            JourneyWorld.start(this, difficulty, hardcore);
        }).bounds(x, y + 64, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"), b -> onClose()).bounds(x, y + 90, 220, 20).build());
    }
    private Component modeLabel() { return Component.literal("Modo: " + (hardcore ? "Hardcore" : "Sobreviv\u00eancia")); }
    private Component difficultyLabel() {
        String label = switch (hardcore ? Difficulty.HARD : difficulty) {
            case PEACEFUL -> "Pac\u00edfico";
            case EASY -> "F\u00e1cil";
            case NORMAL -> "Normal";
            case HARD -> "Dif\u00edcil";
        };
        return Component.literal("Dificuldade: " + label);
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float a) {
        g.fill(0, 0, width, height, 0xff19191b);
        for (int x = 0; x < width; x += 18) g.fill(x, 0, x + 1, height, 0xff302d32);
        for (int y = 0; y < height; y += 18) g.fill(0, y, width, y + 1, 0xff302d32);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
        g.centeredText(font, title, width / 2, height / 2 - 65, 0xffc2a8e3);
        super.extractRenderState(g, mx, my, a);
    }
}
