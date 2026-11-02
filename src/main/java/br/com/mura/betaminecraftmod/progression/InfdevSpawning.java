package br.com.mura.betaminecraftmod.progression;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Reimplemented 20100617 spawning attempts; modern entity AI and collision remain. */
@EventBusSubscriber(modid = "betaminecraftmod")
public final class InfdevSpawning {
    private static final EntityType<?>[] MONSTERS = {net.minecraft.world.entity.EntityTypes.ZOMBIE, net.minecraft.world.entity.EntityTypes.SKELETON, net.minecraft.world.entity.EntityTypes.CREEPER, net.minecraft.world.entity.EntityTypes.SPIDER};
    private static final EntityType<?>[] ANIMALS = {net.minecraft.world.entity.EntityTypes.PIG, net.minecraft.world.entity.EntityTypes.SHEEP};
    private static final EntityType<?>[] ALPHA_ANIMALS = {net.minecraft.world.entity.EntityTypes.PIG, net.minecraft.world.entity.EntityTypes.SHEEP, net.minecraft.world.entity.EntityTypes.COW, net.minecraft.world.entity.EntityTypes.CHICKEN};
    private static final EntityType<?>[] ALPHA_MONSTERS = {net.minecraft.world.entity.EntityTypes.ZOMBIE, net.minecraft.world.entity.EntityTypes.SKELETON, net.minecraft.world.entity.EntityTypes.CREEPER, net.minecraft.world.entity.EntityTypes.SPIDER, net.minecraft.world.entity.EntityTypes.SLIME};
    @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !InfdevContent.legacy(level)
                || !level.getGameRules().get(GameRules.SPAWN_MOBS) || level.players().isEmpty()) return;
        int monsters = 0, animals = 0;
        for (var entity : level.getAllEntities()) {
            if (entity.getType().getCategory() == MobCategory.MONSTER) monsters++;
            if (entity.getType().getCategory() == MobCategory.CREATURE) animals++;
        }
        boolean alpha = CampaignProgress.get(level).era() == 2;
        if (level.getDifficulty() != Difficulty.PEACEFUL && monsters < (alpha ? 200 : 100) && level.getRandom().nextInt(5) == 1)
            attempt(level, alpha ? ALPHA_MONSTERS : MONSTERS, (alpha ? 200 : 100) - monsters, false);
        int animalCap = alpha ? 15 : CampaignProgress.get(level).era() == 1 ? 20 : 50;
        if (animals < animalCap && level.getRandom().nextInt(5) == 1) attempt(level, alpha ? ALPHA_ANIMALS : ANIMALS, animalCap - animals, true);
    }
    private static void attempt(ServerLevel level, EntityType<?>[] types, int remaining, boolean animal) {
        // Multiplayer adaptation: one random eligible player, with global historical caps.
        var players = level.players().stream().filter(p -> !p.isSpectator()).toList();
        if (players.isEmpty()) return;
        var player = players.get(level.getRandom().nextInt(players.size()));
        var type = types[level.getRandom().nextInt(types.length)];
        int originX = player.blockPosition().getX() + level.getRandom().nextInt(256) - 128;
        int y = level.getRandom().nextInt(128);
        int originZ = player.blockPosition().getZ() + level.getRandom().nextInt(256) - 128;
        var start = new BlockPos(originX, y, originZ);
        if (!level.hasChunkAt(start) || !level.getBlockState(start).isAir()) return;
        for (int group = 0; group < 4 && remaining > 0; group++) {
            int x = originX, z = originZ;
            for (int attempt = 0; attempt < 4 && remaining > 0; attempt++) {
                x += level.getRandom().nextInt(6) - level.getRandom().nextInt(6);
                z += level.getRandom().nextInt(6) - level.getRandom().nextInt(6);
                var pos = new BlockPos(x, y, z);
                if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)
                        || !level.getBlockState(pos.below()).isSolidRender() || !level.getBlockState(pos).isAir()
                        || !level.getBlockState(pos.above()).isAir()) continue;
                // Modern entities use feet coordinates rather than the old position convention.
                var feet = pos;
                if (level.getNearestPlayer(x + .5, feet.getY(), z + .5, 16, false) != null) continue;
                int light = level.getMaxLocalRawBrightness(feet);
                if (animal ? light <= 8 : light > level.getRandom().nextInt(8)) continue;
                if (type == net.minecraft.world.entity.EntityTypes.SLIME && feet.getY() >= 16) continue;
                var entity = type.create(level, EntitySpawnReason.NATURAL);
                if (!(entity instanceof Mob mob)) continue;
                mob.snapTo(x + .5, feet.getY(), z + .5, level.getRandom().nextFloat() * 360, 0);
                if (!level.noCollision(mob) || level.containsAnyLiquid(mob.getBoundingBox())) continue;
                // Avoid modern equipment, babies and regional spawn variants from finalizeSpawn.
                if (level.addFreshEntity(mob)) remaining--;
            }
        }
    }
}
