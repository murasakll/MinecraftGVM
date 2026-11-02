package br.com.mura.betaminecraftmod;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** The site's notebook theme, with vanilla actions retained behind each control. */
@EventBusSubscriber(modid = BetaMinecraftMod.MOD_ID, value = Dist.CLIENT)
public final class NotebookMenu extends TitleScreen {
    private static final FontDescription HAND = new FontDescription.Resource(
        Identifier.fromNamespaceAndPath(BetaMinecraftMod.MOD_ID, "handwritten"));

    private int top;

    @SubscribeEvent
    public static void opening(ScreenEvent.Opening event) {
        if (event.getNewScreen() != null && event.getNewScreen().getClass() == TitleScreen.class) {
            JourneyWorld.loading = false;
            event.setNewScreen(new NotebookMenu());
        }
    }

    private int paper() { return 0xff19191b; }
    private int ink() { return 0xffeae7e2; }
    private int accent() { return 0xffc2a8e3; }
    private int line() { return 0xff434041; }
    private Component hand(Component text) { return text.copy().withStyle(style -> style.withFont(HAND)); }

    private Component compactLabel(Component message) {
        if (!(message.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translation)) return message;
        String label = switch (translation.getKey()) {
            case "menu.singleplayer" -> "Go back in the days";
            case "menu.multiplayer" -> "Multijogador";
            case "menu.online" -> "Minecraft Realms";
            case "options.language" -> "Idioma";
            case "options.accessibility" -> "Acessibilidade";
            case "menu.options" -> "Op\u00e7\u00f5es";
            case "menu.quit" -> "Sair";
            case "title.credits" -> "Cr\u00e9ditos";
            default -> message.getString();
        };
        return Component.literal(label);
    }

    @Override
    protected void init() {
        JourneyWorld.loading = false;
        super.init();
        List<Button> originals = children().stream().filter(Button.class::isInstance).map(Button.class::cast).filter(b -> !b.getMessage().getString().equals("TW")).toList();
        clearWidgets();
        int menuWidth = Math.min(width - 32, 220);
        int left = (width - menuWidth) / 2;
        int rows = 3 + (Math.max(0, originals.size() - 3) + 1) / 2;
        top = Math.max(46, (height - rows * 24) / 2 + 14);
        for (int i = 0; i < originals.size(); i++) {
            Button source = originals.get(i);
            int row = i < 3 ? i : 3 + (i - 3) / 2;
            int x = i < 3 ? left : left + ((i - 3) % 2) * (menuWidth / 2 + 2);
            int w = i < 3 ? menuWidth : menuWidth / 2 - 2;
            addRenderableWidget(new PaperButton(x, top + row * 24, w, 20, source));
        }
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int x, int y, float a) { }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        g.fill(0, 0, width, height, paper());
        for (int x = 0; x < width; x += 18) g.fill(x, 0, x + 1, height, line());
        for (int y = 0; y < height; y += 18) g.fill(0, y, width, y + 1, line());
        Component title = hand(Component.literal("Beta Minecraft Mod"));
        g.text(font, title, (width - font.width(title)) / 2, top - 28, ink(), false);
        for (var child : children()) {
            if (child instanceof PaperButton button) button.extractRenderState(g, mouseX, mouseY, a);
        }
    }

    private final class PaperButton extends Button {
        private final Button original;
        PaperButton(int x, int y, int w, int h, Button original) {
            super(x, y, w, h, hand(compactLabel(original.getMessage())), b -> {},
                supplier -> original.getMessage().copy());
            this.original = original;
            active = original.active;
        }
        @Override public void onPress(InputWithModifiers input) {
            if (original.getMessage().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translation
                    && translation.getKey().equals("menu.singleplayer")) JourneyWorld.openEntry(NotebookMenu.this);
            else original.onPress(input);
        }
        @Override protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float a) {
            boolean hover = isHoveredOrFocused();
            g.fill(getX(), getY(), getX() + width, getY() + height,
                hover ? 0xff342d3c : 0xff202022);
            g.fill(getX(), getY() + height - 1, getX() + width, getY() + height, hover ? accent() : line());
            Component label = getMessage();
            // Compact labels retain the complete native message in narration.
            if (font.width(label) > width - 8) {
                g.text(font, net.minecraft.locale.Language.getInstance().getVisualOrder(font.substrByWidth(label, width - 8)), getX() + 4, getY() + 4,
                    active ? ink() : 0xff888888, false);
            } else {
                g.text(font, label, getX() + (width - font.width(label)) / 2, getY() + 5, active ? ink() : 0xff888888, false);
            }
        }
    }
}

