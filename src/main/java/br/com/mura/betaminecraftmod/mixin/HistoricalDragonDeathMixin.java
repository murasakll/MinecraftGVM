package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.world.HistoricalEndChunkGenerator;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

/** The original death sequence awards ten 1000-XP waves and a final 10000 XP. */
@Mixin(EnderDragon.class)
public abstract class HistoricalDragonDeathMixin {
    @Unique private boolean campaign$originalDeath() {
        var dragon=(EnderDragon)(Object)this;
        return CampaignRules.era(dragon.level())==6&&dragon.level() instanceof net.minecraft.server.level.ServerLevel server&&server.getChunkSource().getGenerator() instanceof HistoricalEndChunkGenerator;
    }
    // Adjust the base amount before NeoForge's drop hook, retaining gamerules and other mods' vetoes.
    @ModifyArg(method="tickDeath",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/event/EventHooks;getExperienceDrop(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;I)I",ordinal=0),index=2)
    private int campaign$originalWave(int amount){return campaign$originalDeath()?1000:amount;}
    @ModifyArg(method="tickDeath",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/event/EventHooks;getExperienceDrop(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;I)I",ordinal=1),index=2)
    private int campaign$originalFinalAward(int amount){return campaign$originalDeath()?10000:amount;}
}
