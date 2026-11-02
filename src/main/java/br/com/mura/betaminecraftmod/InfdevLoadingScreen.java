package br.com.mura.betaminecraftmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Render only: the real loading screen keeps its tracker, tick, narration and readiness checks. */
@EventBusSubscriber(modid = BetaMinecraftMod.MOD_ID, value = Dist.CLIENT)
public final class InfdevLoadingScreen {
    private static final Identifier DIRT = Identifier.fromNamespaceAndPath(BetaMinecraftMod.MOD_ID, "textures/gui/infdev_dirt.png");
    private InfdevLoadingScreen() { }

    @SubscribeEvent
    public static void render(ScreenEvent.Render.Pre event) {
        if (!JourneyWorld.loading && event.getScreen() instanceof LevelLoadingScreen) {
            var server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null && server.overworld() != null && br.com.mura.betaminecraftmod.progression.CampaignRules.isCampaign(server.overworld())) {
                JourneyWorld.loading = true;
                JourneyWorld.creating = false;
            }
        }
        if (!JourneyWorld.loading) return;
        var screen = event.getScreen();
        float progress = -1;
        String stage;
        if (screen instanceof LevelLoadingScreen level) {
            progress = level.loadTracker.serverProgress();
            stage = progress < 1 ? "Preparing chunks" : "Preparing lights";
        } else if (screen instanceof GenericMessageScreen && screen.getTitle().getContents()
                instanceof net.minecraft.network.chat.contents.TranslatableContents title
                && title.getKey().equals("selectWorld.data_read")) stage = "Preparing chunks";
        else return;

        draw(event.getGuiGraphics(),screen.width,screen.height,progress,stage,JourneyWorld.creating);
        event.setCanceled(true);
    }

    public static void draw(GuiGraphicsExtractor g, int width, int height, float progress, String stage, boolean creating) {
        for (int x = 0; x < width; x += 32) for (int y = 0; y < height; y += 32)
            g.blit(RenderPipelines.GUI_TEXTURED, DIRT, x, y, 0, 0, 32, 32, 32, 32, 0xff404040);
        var font = Minecraft.getInstance().font;
        g.centeredText(font, Component.literal(creating ? "Generating level" : "Loading level"), width / 2, height / 2 - 20, 0xffffffff);
        g.centeredText(font, Component.literal(stage), width / 2, height / 2 + 4, 0xffffffff);
        if (progress >= 0) {
            int x = width / 2 - 50, y = height / 2 + 16;
            g.fill(x, y, x + 100, y + 2, 0xff808080);
            g.fill(x, y, x + Math.round(Math.clamp(progress, 0, 1) * 100), y + 2, 0xff80ff80);
        }
    }
}
