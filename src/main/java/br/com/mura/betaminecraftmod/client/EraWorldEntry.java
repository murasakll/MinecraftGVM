package br.com.mura.betaminecraftmod.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

/** Local-save hints prepare resources; only the later server payload activates gameplay rules. */
public final class EraWorldEntry {
    private static final EraEntryState STATE=new EraEntryState();
    private static long requestToken;
    public static boolean entryInProgress() { return STATE.inProgress(); }
    public static void confirmed() { STATE.clear(); }
    public static void cancelled() { requestToken++; ready=null; STATE.clear(); EraTextures.select(-1); }
    private EraWorldEntry() { }
    public static void open(String levelId, Runnable load) {
        var minecraft = Minecraft.getInstance();
        Path root = minecraft.getLevelSource().getLevelPath(levelId);
        prepare(CompletableFuture.supplyAsync(() -> savedEra(root,levelId),Util.backgroundExecutor()),load);
    }
    public static void prepareNewWorld(Runnable load) { prepare(CompletableFuture.completedFuture(0),load); }
    private static int savedEra(Path root, String levelId) {
        for (String path : new String[]{"dimensions/minecraft/overworld/data/betaminecraftmod/campaign.dat", "data/betaminecraftmod/campaign.dat"}) {
            Path file = root.resolve(path);
            if (!Files.isRegularFile(file)) continue;
            try {
                var data = NbtIo.readCompressed(file,NbtAccounter.create(2 * 1024 * 1024)).getCompoundOrEmpty("data");
                int era = data.getIntOr("era",0);
                if (br.com.mura.betaminecraftmod.progression.EraCatalog.get(era) == null) throw new IllegalStateException("Unknown saved era: " + era);
                return era;
            } catch (java.io.IOException error) { throw new java.util.concurrent.CompletionException(error); }
        }
        return levelId.startsWith("betaminecraftmod-journey-") ? 0 : -1;
    }
    private static void prepare(CompletableFuture<Integer> era, Runnable load) {
        if (!STATE.begin()) return;
        long token=++requestToken;
        var minecraft=Minecraft.getInstance();
        Screen parent=minecraft.gui.screen();
        minecraft.gui.setScreen(new PreparationScreen());
        era.thenComposeAsync(value -> token == requestToken ? EraTextures.prepare(value) : CompletableFuture.completedFuture(null),minecraft).whenCompleteAsync((unused,error) -> {
            if (token != requestToken) return;
            if (error != null) {
                STATE.clear();
                com.mojang.logging.LogUtils.getLogger().error("Could not prepare world resources",error);
                minecraft.gui.setScreen(new DisconnectedScreen(parent,Component.literal("Falha ao preparar recursos"),Component.literal("Nao foi possivel carregar os recursos da era. Consulte o log.")));
            } else waitUntilOverlayCloses(load);
        },minecraft);
    }
    private static Runnable ready;
    private static void waitUntilOverlayCloses(Runnable load) {
        ready=load;
        finishIfReady();
    }
    public static void finishIfReady() {
        if (ready != null && Minecraft.getInstance().gui.overlay() == null) {
            Runnable load=ready; ready=null; STATE.joining(); load.run();
        }
    }
    private static final class PreparationScreen extends Screen {
        PreparationScreen() { super(Component.literal("Loading level")); }
        @Override public boolean shouldCloseOnEsc() { return false; }
        @Override public void extractRenderState(GuiGraphicsExtractor graphics,int x,int y,float delta) {
            br.com.mura.betaminecraftmod.InfdevLoadingScreen.draw(graphics,width,height,-1,"Preparing resources",false);
        }
    }
}
