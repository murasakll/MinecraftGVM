package br.com.mura.betaminecraftmod.client;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.quests.QuestActionPayload;
import br.com.mura.betaminecraftmod.quests.QuestProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Native Minecraft advancement tree; quest conditions and rewards remain server authoritative. */
@EventBusSubscriber(modid = "betaminecraftmod", value = Dist.CLIENT)
public final class QuestScreen extends AdvancementsScreen {
    public static QuestProgress progress = QuestProgress.EMPTY;
    public static boolean synchronizedState;
    public static int chapterEra = -1;
    public static br.com.mura.betaminecraftmod.quests.TreeQuestStatePayload treeState;
    private static br.com.mura.betaminecraftmod.quests.QuestChapter chapter() { return br.com.mura.betaminecraftmod.quests.QuestChapter.get(chapterEra); }
    private Button claim;
    public QuestScreen(Screen parent) { super(Minecraft.getInstance().getConnection().getAdvancements(), parent); }
    private static boolean nextEraReady() { var next = br.com.mura.betaminecraftmod.progression.EraCatalog.get(chapterEra + 1); return next != null && next.playable(); }
    @SubscribeEvent public static void opening(ScreenEvent.Opening event) {
        var player = Minecraft.getInstance().player;
        if (event.getNewScreen() != null && event.getNewScreen().getClass() == AdvancementsScreen.class
                && player != null && br.com.mura.betaminecraftmod.quests.QuestChapter.get(CampaignRules.era(player)) != null)
            event.setNewScreen(new QuestScreen(event.getCurrentScreen()));
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { progress = QuestProgress.EMPTY; synchronizedState = false; chapterEra = -1; treeState=null; }
    @SubscribeEvent public static void buttons(ScreenEvent.Init.Post event) {
        var player = Minecraft.getInstance().player;
        if (player == null || !CampaignRules.legacyFood(player)) return;
        for (var child : event.getScreen().children()) {
            if (event.getScreen() instanceof net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen
                    && child instanceof net.minecraft.client.gui.components.ImageButton image) { image.visible = false; image.active = false; }
            if (child instanceof Button button && button.getMessage().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text
                    && text.getKey().equals("gui.advancements")) button.setMessage(Component.literal("Objetivos"));
        }
    }
    @Override protected void init() {
        super.init();
        claim = addRenderableWidget(Button.builder(Component.empty(), button -> {
            button.active = false;
            var tree=treeState;
            if(tree!=null&&tree.era()==CampaignRules.era(minecraft.player))
                ClientPacketDistributor.sendToServer(new QuestActionPayload(tree.era(),tree.phase()>0?-12:tree.won()?-11:-10,true));
        }).bounds((width - 248) / 2, Math.max(0, height - 50), 248, 20).build());
        updateClaim();
        ClientPacketDistributor.sendToServer(new QuestActionPayload(CampaignRules.era(minecraft.player), -1, false));
    }
    @Override public void tick() { super.tick(); updateClaim(); }
    private void updateClaim() {
        if (claim == null) return;
        if(minecraft.player!=null&&!br.com.mura.betaminecraftmod.quests.QuestTree.goals(CampaignRules.era(minecraft.player)).isEmpty()){
            updateTree();return;
        }
        boolean finished = progress.stage() == 4;
        claim.setMessage(Component.literal(!synchronizedState ? "Carregando objetivos..." : finished ? "Avançar de era" : "Concluir objetivo: " + (chapter() == null ? "" : chapter().counter(progress))));
        claim.active = synchronizedState && minecraft.player != null && chapterEra == CampaignRules.era(minecraft.player) && chapter() != null && (finished ? nextEraReady() : chapter().ready(progress));
        claim.setTooltip(Tooltip.create(Component.literal(finished
            ? nextEraReady() ? "Avan\u00e7ar libera o pr\u00f3ximo cap\u00edtulo e amplia a borda. Voc\u00ea tamb\u00e9m pode continuar nesta era." : "Voc\u00ea pode continuar nesta era. O pr\u00f3ximo cap\u00edtulo ainda est\u00e1 em desenvolvimento."
            : (chapter() == null ? "" : chapter().description(progress.stage())) + " Recompensa: " + (chapter() == null ? "" : chapter().reward(progress.stage())))));
        for (var child : children()) if (child instanceof Button button && button != claim)
            button.setMessage(finished ? Component.literal("Continuar nesta era") : Component.translatable("gui.done"));
    }
    private void updateTree(){
        var tree=treeState;int era=CampaignRules.era(minecraft.player);
        if(tree==null||tree.era()!=era){claim.setMessage(Component.literal("Carregando desafios..."));claim.active=false;return;}
        var goals=br.com.mura.betaminecraftmod.quests.QuestTree.goals(era);var completed=br.com.mura.betaminecraftmod.quests.QuestTree.fromMask(era,tree.mask());
        long required=goals.stream().filter(q->!q.optional()).count(),count=goals.stream().filter(q->!q.optional()&&completed.contains(q.id())).count();
        boolean ready=br.com.mura.betaminecraftmod.quests.QuestTree.ready(era,completed),running=tree.phase()>0;
        claim.setMessage(Component.literal(running?tree.phase()==3?"Cerco em andamento":"Encerrar tentativa · onda "+tree.wave():tree.won()?"Confirmar avanço de era":ready?"Iniciar cerco de zumbis":"Preparação: "+count+" / "+required+" objetivos"));
        var next=br.com.mura.betaminecraftmod.progression.EraCatalog.get(era+1);
        claim.active=running?tree.phase()!=3:tree.won()?tree.advanceReady():ready;
        StringBuilder text=new StringBuilder("Preparação, Exploração, Combate e Construção convergem para o cerco.\n");
        for(String branch:new String[]{"prep","explore","combat","build"}){long total=goals.stream().filter(q->q.branch().equals(branch)&&!q.optional()).count(),done=goals.stream().filter(q->q.branch().equals(branch)&&completed.contains(q.id())).count();text.append(switch(branch){case "prep"->"Preparação";case "explore"->"Exploração";case "combat"->"Combate";default->"Construção";}).append(": ").append(done).append(" / ").append(total).append('\n');}
        var plan=br.com.mura.betaminecraftmod.quests.QuestTree.siege(era);text.append("Cerco: ").append(plan.waves()).append(" ondas / ").append(plan.total()).append(" zumbis. Início manual; dificuldade Fácil ou superior.\n");
        if(running)text.append("Onda ").append(tree.wave()).append(" | vivos: ").append(tree.living()).append(" | ainda vão aparecer: ").append(tree.remaining()).append(". Encerrar preserva quests, mas exige vencer uma nova tentativa.\n");
        if(tree.won())text.append(tree.advanceReady()?"Avançar confirma a expansão. Você também pode continuar nesta era.":era==5&&next!=null&&next.playable()?"Campanha antiga sem garantia do portal: use um mundo novo para seguir até o lançamento.":era==6?"Você concluiu o lançamento 1.0. Pode continuar nesta era; os próximos capítulos ainda estão em desenvolvimento.":"Cerco concluído. A próxima era ainda está em desenvolvimento.");
        claim.setTooltip(Tooltip.create(Component.literal(text.toString())));
        for(var child:children())if(child instanceof Button button&&button!=claim)button.setMessage(Component.literal(tree.won()?"Continuar nesta era":"Voltar ao jogo"));
    }
}
