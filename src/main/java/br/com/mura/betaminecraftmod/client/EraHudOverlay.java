package br.com.mura.betaminecraftmod.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = "betaminecraftmod", value = Dist.CLIENT)
public final class EraHudOverlay {
    private static final int MARGIN = 4;
    @SubscribeEvent public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("betaminecraftmod", "era_name"), EraHudOverlay::render);
    }
    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.gui.hud.isHidden() || !ClientEraState.active()) return;
        var text = ClientEraState.name();
        graphics.text(minecraft.font, text, graphics.guiWidth() - minecraft.font.width(text) - MARGIN, MARGIN, 0xffffffff, true);
    }
}
