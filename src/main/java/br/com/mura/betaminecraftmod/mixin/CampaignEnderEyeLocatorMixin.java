package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import br.com.mura.betaminecraftmod.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class CampaignEnderEyeLocatorMixin {
    @Inject(method="findNearestMapStructure(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;",at=@At("HEAD"),cancellable=true)
    private void campaign$locatePortal(TagKey<Structure> tag,BlockPos origin,int radius,boolean reference,CallbackInfoReturnable<BlockPos> ci){
        var level=(ServerLevel)(Object)this;if(!tag.equals(StructureTags.EYE_OF_ENDER_LOCATED)||!CampaignRules.isCampaign(level))return;
        var generator=(InfdevChunkGenerator)level.getServer().overworld().getChunkSource().getGenerator();
        if(CampaignRules.era(level)==6&&level.dimension().equals(Level.OVERWORLD)&&generator.hasReleasePortal()){var plan=generator.releasePortalPlan();ci.setReturnValue(new BlockPos(plan.x(),ReleasePortalPlan.PORTAL_Y,plan.z()));}
        else if(CampaignRules.era(level)<=6)ci.setReturnValue(null);
    }
}
