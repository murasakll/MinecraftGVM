package br.com.mura.betaminecraftmod.progression;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "betaminecraftmod")
public final class CampaignEvents {
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        if (CampaignRules.isCampaign(level)) {
            CampaignProgress.get(level).applyBorder(level);
            level.getGameRules().set(net.minecraft.world.level.gamerules.GameRules.RESPAWN_RADIUS, 0, event.getServer());
        }
    }
    @SubscribeEvent public static void joined(PlayerEvent.PlayerLoggedInEvent event) { sync(event); }
    @SubscribeEvent public static void respawned(PlayerEvent.PlayerRespawnEvent event) { sync(event); }
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) { sync(event); }
    private static void sync(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int era = CampaignRules.isCampaign(player.level()) ? CampaignProgress.get(player.level()).era() : -1;
            PacketDistributor.sendToPlayer(player, EraPayload.forLevel(era,player.level()));
            if (era >= 0 && era < CampaignRules.ADVENTURE_UPDATE) player.setSprinting(false);
        }
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("journey")
            .then(Commands.literal("status").executes(context -> {
                ServerLevel level = context.getSource().getLevel();
                if (!CampaignRules.isCampaign(level)) return 0;
                var progress = CampaignProgress.get(level);
                context.getSource().sendSuccess(() -> Component.literal(EraCatalog.get(progress.era()).reference() + " | faixa " + progress.rings() + " | area " + ExplorationBands.diameter(progress.rings()) + " x " + ExplorationBands.diameter(progress.rings())), false);
                return 1;
            }))
            .then(Commands.literal("skip").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(context -> {
                var player = context.getSource().getPlayerOrException();
                if (!CampaignRules.isCampaign(player.level())) {
                    context.getSource().sendFailure(Component.literal("Este comando so funciona em um mundo da jornada."));
                    return 0;
                }
                if (!br.com.mura.betaminecraftmod.quests.QuestService.skipAndAdvance(player)) {
                    context.getSource().sendFailure(Component.literal("A proxima era ainda nao esta jogavel. Nenhum desafio ou terreno foi alterado."));
                    return 0;
                }
                context.getSource().sendSuccess(() -> Component.literal("Desafios pulados. Era atual: " + EraCatalog.get(CampaignProgress.get(player.level()).era()).reference()), true);
                return 1;
            }))
            .then(Commands.literal("expand").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(context -> {
                ServerLevel level = context.getSource().getLevel();
                if (!CampaignRules.isCampaign(level)) return 0;
                if (!CampaignProgress.get(level).unlockRing(level)) {
                    context.getSource().sendFailure(Component.literal("A proxima faixa ainda aguarda seu gerador historico. Nenhum chunk foi alterado."));
                    return 0;
                }
                context.getSource().sendSuccess(() -> Component.literal("Nova faixa liberada: +512 blocos por lado. " + EraCatalog.get(CampaignProgress.get(level).era()).reference()), true);
                return 1;
            })));
    }
}
