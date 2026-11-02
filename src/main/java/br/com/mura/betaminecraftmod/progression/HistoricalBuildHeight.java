package br.com.mura.betaminecraftmod.progression;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** The taller campaign container must not grant historical eras a modern build ceiling. */
@EventBusSubscriber(modid="betaminecraftmod")
public final class HistoricalBuildHeight {
    private HistoricalBuildHeight() { }
    public static boolean allowed(int era,int y){return era<0 || era>5 || y>=0 && y<128;}
    public static boolean allowed(int era,int y,String firstRelease){return era==6&&!firstRelease.equals("1.2.5")?y>=0&&y<128:allowed(era,y);}
    @SubscribeEvent public static void place(BlockEvent.EntityPlaceEvent event){
        if(!(event.getLevel() instanceof ServerLevel level) || !CampaignRules.isCampaign(level))return;
        int era=CampaignProgress.get(level).era();
        String release=((br.com.mura.betaminecraftmod.world.InfdevChunkGenerator)level.getServer().overworld().getChunkSource().getGenerator()).firstRelease();
        if(!allowed(era,event.getPos().getY(),release)){event.setCanceled(true);return;}
        if(event instanceof BlockEvent.EntityMultiPlaceEvent multi)
            for(var snapshot:multi.getReplacedBlockSnapshots())
                if(!allowed(era,snapshot.getPos().getY(),release)){event.setCanceled(true);return;}
    }
}
