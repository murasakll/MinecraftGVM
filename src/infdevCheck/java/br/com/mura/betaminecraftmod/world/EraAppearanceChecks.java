package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.*;
import java.util.*;

final class EraAppearanceChecks {
    static void run(){
        if(EraAppearanceCatalog.all().size()!=20||EraAppearanceCatalog.get(-1)!=null||EraAppearanceCatalog.get(20)!=null)throw new AssertionError("Appearance IDs changed");
        var identities=new HashSet<String>();
        for(int era=0;era<20;era++) {
            var p=EraAppearanceCatalog.get(era);
            if(p.era()!=era||!identities.add(p.textureSet())||p.checkpoints().isEmpty())throw new AssertionError("Collapsed texture reference "+era);
            if(era>=3&&era<=6&&(p.state()!=EraAppearanceCatalog.State.STAGED_PARTIAL||p.packs().size()!=(era<=4?2:1)))throw new AssertionError("Partial sprite preview missing");
            if(era>6&&era<19&&(p.state()!=EraAppearanceCatalog.State.PENDING||!p.packs().isEmpty()))throw new AssertionError("Unverified era inherited old textures");
            if(!EraAppearanceCatalog.managedTexturePacks().containsAll(p.packs()))throw new AssertionError("Pack cannot be removed on transition");
            if(p.state()==EraAppearanceCatalog.State.PENDING||p.state()==EraAppearanceCatalog.State.STAGED_PARTIAL) {
                var d=EraCatalog.get(era);
                if(d.playable()!=(d.terrainReady()&&d.questsReady()))throw new AssertionError("Cosmetic completeness replaced campaign readiness "+era);
            }
        }
        if(!EraCatalog.get(0).resourcePacks().equals(List.of("resourcepacks/infdev","resourcepacks/infdev_audio"))
            ||!EraCatalog.get(1).resourcePacks().equals(List.of("resourcepacks/infdev","resourcepacks/infdev_audio","resourcepacks/infdev_late"))
            ||!EraCatalog.get(2).resourcePacks().equals(List.of("resourcepacks/infdev","resourcepacks/infdev_audio","resourcepacks/alpha_early")))throw new AssertionError("Existing pack order changed");
        if(!EraAppearanceCatalog.get(11).checkpoints().contains("1.14 Texture Update")||!EraAppearanceCatalog.get(6).checkpoints().contains("1.0"))throw new AssertionError("Important visual substep missing");
        if(!EraCatalog.get(6).resourcePacks("1.1").isEmpty()||!EraCatalog.get(6).resourcePacks("1.2.5").isEmpty())throw new AssertionError("Release 1.0 pixels leaked into an unverified later substep");
        System.out.println("ERA_APPEARANCE SUCCESS: 20 independent references, explicit pending states, visual substeps and existing pack order");
    }
}
