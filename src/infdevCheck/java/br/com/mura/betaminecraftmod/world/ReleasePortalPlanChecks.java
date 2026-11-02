package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.ExplorationBands;
import java.util.*;

final class ReleasePortalPlanChecks {
    static void run() {
        for(long seed=0;seed<1024;seed++){
            var plan=ReleasePortalPlan.forSeed(seed);
            if(!plan.equals(ReleasePortalPlan.forSeed(seed)))throw new AssertionError("Unstable portal landmark");
            if(ExplorationBands.chunkBand(plan.chunkX(),plan.chunkZ())!=6)throw new AssertionError("Portal outside release band");
            if(seed>=64)continue;
            for(int surface:new int[]{0,48,64,120,127}){
                var cells=new HashMap<ReleasePortalPlan.Pos,ReleasePortalPlan.Kind>();
                for(var cell:plan.layout(surface)){
                    var p=cell.getKey();cells.put(p,cell.getValue());
                    if(!plan.owns(Math.floorDiv(p.x(),16),Math.floorDiv(p.z(),16))||!plan.protectedCell(p.x(),p.y(),p.z())||Math.abs(p.x())>=3572||Math.abs(p.z())>=3572)throw new AssertionError("Landmark crosses protected footprint or border");
                }
                int frames=0;for(var kind:cells.values())if(kind.name().startsWith("FRAME_"))frames++;
                if(frames!=12)throw new AssertionError("Incomplete portal ring");
                for(int offset=-1;offset<=1;offset++){
                    require(cells,plan,offset,39,-2,ReleasePortalPlan.Kind.FRAME_SOUTH);
                    require(cells,plan,offset,39,2,ReleasePortalPlan.Kind.FRAME_NORTH);
                    require(cells,plan,-2,39,offset,ReleasePortalPlan.Kind.FRAME_EAST);
                    require(cells,plan,2,39,offset,ReleasePortalPlan.Kind.FRAME_WEST);
                    for(int z=-1;z<=1;z++){require(cells,plan,offset,39,z,ReleasePortalPlan.Kind.AIR);require(cells,plan,offset,38,z,ReleasePortalPlan.Kind.BRICK);}
                }
                int entry=Math.clamp(surface,48,120);
                for(int y=39;y<=entry+1;y++){require(cells,plan,-4,y,-4,ReleasePortalPlan.Kind.LADDER_EAST);require(cells,plan,-5,y,-4,ReleasePortalPlan.Kind.BRICK);}
                for(int y:new int[]{39,40,entry,entry+1})require(cells,plan,-3,y,-4,ReleasePortalPlan.Kind.AIR);
                if(plan.protectedCell(plan.x()+7,39,plan.z())||plan.protectedCell(plan.x(),128,plan.z()))throw new AssertionError("Protection escaped historical footprint");
            }
        }
        System.out.println("RELEASE_PORTAL_PLAN SUCCESS: 1024 seeds, release-only band, inward frames, empty center, supported ladder and protected single-chunk footprint");
    }
    private static void require(Map<ReleasePortalPlan.Pos,ReleasePortalPlan.Kind> cells,ReleasePortalPlan plan,int x,int y,int z,ReleasePortalPlan.Kind kind){if(cells.get(new ReleasePortalPlan.Pos(plan.x()+x,y,plan.z()+z))!=kind)throw new AssertionError("Invalid portal cell "+x+","+y+","+z);}
}
