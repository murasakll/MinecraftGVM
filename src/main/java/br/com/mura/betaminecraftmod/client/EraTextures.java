package br.com.mura.betaminecraftmod.client;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = "betaminecraftmod", value = Dist.CLIENT)
public final class EraTextures {
    private static final java.util.List<String> PACKS = java.util.stream.Stream.concat(
        br.com.mura.betaminecraftmod.progression.EraAppearanceCatalog.managedTexturePacks().stream(),
        java.util.stream.Stream.of("resourcepacks/infdev_audio")).distinct().toList();
    private static int selectedEra = -1;
    private static final java.util.List<java.util.concurrent.CompletableFuture<Void>> OVERLAY_WAITERS = new java.util.ArrayList<>();
    private static final EraResourceReloadQueue RELOADS = new EraResourceReloadQueue(EraTextures::applyResources, task -> Minecraft.getInstance().execute(task));
    private static net.minecraft.client.gui.screens.Overlay ownedOverlay;
    private static java.util.concurrent.CompletableFuture<Void> reloadResult;
    public static void reloadFailed(Object overlay, Throwable error) {
        if (ownsOverlay(overlay) && reloadResult != null) reloadResult.completeExceptionally(error);
    }
    public static boolean ownsOverlay(Object overlay) { return overlay != null && overlay == ownedOverlay; }
    public static boolean preparing() { return RELOADS.busy(); }
    public static boolean blockWorldRendering() { return preparing() || ownsOverlay(Minecraft.getInstance().gui.overlay()); }
    public static java.util.concurrent.CompletableFuture<Void> prepare(int era) { return RELOADS.request(era); }
    private static java.util.concurrent.CompletableFuture<Void> awaitOverlay() {
        if (Minecraft.getInstance().gui.overlay() == null) return java.util.concurrent.CompletableFuture.completedFuture(null);
        var future = new java.util.concurrent.CompletableFuture<Void>();
        OVERLAY_WAITERS.add(future);
        return future;
    }
    @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        EraWorldEntry.finishIfReady();
        if (Minecraft.getInstance().gui.overlay() == null && !OVERLAY_WAITERS.isEmpty()) {
            var ready = java.util.List.copyOf(OVERLAY_WAITERS);
            OVERLAY_WAITERS.clear();
            ready.forEach(future -> future.complete(null));
        }
    }
    @SubscribeEvent public static void packs(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) for (String path : PACKS)
            event.addPackFinders(Identifier.fromNamespaceAndPath("betaminecraftmod", path), PackType.CLIENT_RESOURCES,
                Component.literal(path.endsWith("_audio") ? "Infdev - audio" : path.endsWith("visual_release_1_0")?"Release 1.0 sprites (preview)":br.com.mura.betaminecraftmod.progression.EraAppearanceCatalog.all().stream().filter(profile->profile.packs().contains(path)).map(profile->profile.reference()).findFirst().orElse(path)), PackSource.BUILT_IN, false, Pack.Position.TOP);
    }
    public static void select(int era) {
        HistoricalVegetationColors.select(era);
        CampaignRules.clientEra(era);
        ClientEraState.setEra(era);
        br.com.mura.betaminecraftmod.client.EraVisuals.select(era);
        Minecraft minecraft = Minecraft.getInstance();
        if (era != selectedEra) { minecraft.getSoundManager().stop(); selectedEra = era; }
        prepare(era).exceptionally(error -> { com.mojang.logging.LogUtils.getLogger().error("Could not prepare era resources", error); return null; });
    }
    private static java.util.concurrent.CompletableFuture<Void> applyResources(int era) {
        Minecraft minecraft = Minecraft.getInstance();
        return awaitOverlay().thenComposeAsync(ignored -> {
        var repository = minecraft.getResourcePackRepository();
        var profile = br.com.mura.betaminecraftmod.progression.EraCatalog.get(era);
        var wanted = profile == null ? java.util.List.<String>of() : profile.resourcePacks(CampaignRules.firstRelease(minecraft.level));
        boolean changed = false;
        for (String path : PACKS) {
            String id = "mod/betaminecraftmod:" + path;
            changed |= wanted.contains(path) ? repository.addPack(id) : repository.removePack(id);
        }
        if (!changed) return java.util.concurrent.CompletableFuture.completedFuture(null);
        com.mojang.logging.LogUtils.getLogger().debug("Preparing resource selection for era {}",era);
        var reloading = minecraft.reloadResourcePacks();
        ownedOverlay = minecraft.gui.overlay();
        var completion = new java.util.concurrent.CompletableFuture<Void>();
        reloadResult = completion;
        reloading.whenComplete((unused,error) -> {
            if (error == null) completion.complete(null); else completion.completeExceptionally(error);
        });
        // Iris can request this texture lazily inside a render pass. Upload it beforehand.
        return completion.thenRunAsync(() -> minecraft.getTextureManager().getTexture(
            Identifier.withDefaultNamespace("textures/environment/clouds.png")), minecraft);
        }, minecraft);
    }
    @SubscribeEvent public static void cancelled(net.neoforged.neoforge.client.event.ScreenEvent.Opening event) {
        var screen=event.getNewScreen();
        if (EraWorldEntry.entryInProgress() && (screen instanceof net.minecraft.client.gui.screens.TitleScreen
                || screen instanceof net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
                || screen instanceof net.minecraft.client.gui.screens.DisconnectedScreen)) EraWorldEntry.cancelled();
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        // Minecraft disconnects an empty connection while starting its integrated server too.
        // Keep the prepared packs through that internal transition, without activating client rules.
        if (EraWorldEntry.entryInProgress()) {
            HistoricalVegetationColors.select(-1);
            CampaignRules.clientEra(-1); ClientEraState.setEra(-1); EraVisuals.select(-1); selectedEra=-1;
        } else select(-1);
    }
    @SubscribeEvent public static void hud(RenderGuiLayerEvent.Pre event) {
        var player = Minecraft.getInstance().player;
        if (player != null && CampaignRules.legacyFood(player)
                && (event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)
                || event.getName().equals(VanillaGuiLayers.EXPERIENCE_LEVEL)
                || event.getName().equals(VanillaGuiLayers.CONTEXTUAL_INFO_BAR)
                || event.getName().equals(VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND))) event.setCanceled(true);
    }
}
