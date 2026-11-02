package br.com.mura.betaminecraftmod.progression;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;

@EventBusSubscriber(modid = "betaminecraftmod")
public final class OriginSpawn {
    public static BlockPos safePosition(ServerLevel level) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0);
        // Seeds for fresh campaigns are selected for dry land here. Guard later terrain edits too.
        y = Math.clamp(y, level.getMinY() + 1, level.getMaxY() - 2);
        BlockPos pos = new BlockPos(0, y, 0);
        if (!level.getBlockState(pos.below()).getFluidState().isEmpty() || level.getBlockState(pos.below()).isAir())
            level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        if (!level.getBlockState(pos.above()).isAir()) level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
        return pos;
    }
    @SubscribeEvent public static void create(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level && CampaignRules.isCampaign(level)) {
            event.getSettings().setSpawn(LevelData.RespawnData.of(level.dimension(), safePosition(level), 0, 0));
            event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void firstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) || !CampaignRules.isCampaign(player.level())) return;
        if (player.getPersistentData().getBooleanOr("betaminecraftmod_origin_spawned", false)) return;
        var level = player.level().getServer().overworld();
        var pos = safePosition(level);
        player.teleportTo(level, .5, pos.getY(), .5, java.util.Set.of(), 0, 0, true);
        player.getPersistentData().putBoolean("betaminecraftmod_origin_spawned", true);
    }
    @SubscribeEvent public static void respawn(PlayerRespawnPositionEvent event) {
        var player = (net.minecraft.server.level.ServerPlayer) event.getEntity();
        if (!CampaignRules.isCampaign(player.level())) return;
        var level = player.level().getServer().overworld();
        var pos = safePosition(level);
        event.setTeleportTransition(new TeleportTransition(level, Vec3.atBottomCenterOf(pos), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
    }
}
