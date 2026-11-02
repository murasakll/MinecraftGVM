package br.com.mura.betaminecraftmod.world;

import java.security.MessageDigest;
import java.util.*;

final class ReleaseBaseChecks {
    static String hash(TerrainVolume volume) throws Exception {
        var digest=MessageDigest.getInstance("SHA-256");
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=volume.minY();y<volume.minY()+volume.height();y++){
            digest.update(volume.block(x,y,z).getBytes(java.nio.charset.StandardCharsets.UTF_8));digest.update((byte)0);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    static void run() throws Exception {
        if(GenerationBases.all().size()!=20)throw new AssertionError("Missing campaign generation base");
        var bases=new GenerationBases(123456789);
        if(new GenerationBases(123456789,"1.0").generate(6,193,0,null).height()!=128
            ||new GenerationBases(123456789,"1.1").generate(6,193,0,null).height()!=128
            ||bases.generate(6,193,0,null).height()!=256)throw new AssertionError("First release milestone changed height");
        for(String version:new String[]{"1.0","1.1","1.2.5","1.6.4","1.7.10","1.9.4","1.12.2","1.13.2","1.15.2","1.16.5","1.17.1"}){
            TerrainVolume a=bases.release(version,-7,12);String first=hash(a);
            bases.release(version,1,-1);
            if(!first.equals(hash(bases.release(version,-7,12))))throw new AssertionError("Generation order changes release "+version);
            if(first.equals(hash(new GenerationBases(-987654321).release(version,-7,12))))throw new AssertionError("Seed ignored "+version);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)if(!a.block(x,0,z).equals("minecraft:bedrock"))throw new AssertionError("Missing bedrock "+version+" "+x+","+z);
            var columns=new HashSet<String>();
            for(int x=0;x<16;x++)for(int z=0;z<16;z++){
                var signature=new StringBuilder();for(int y=0;y<a.height();y++)signature.append(a.block(x,y,z)).append(';');columns.add(signature.toString());
            }
            if(columns.size()<2)throw new AssertionError("Repeated chunk column "+version);
            if(!a.block(0,-1,0).equals("minecraft:air")||!a.block(0,a.height(),0).equals("minecraft:air"))throw new AssertionError("Vertical leakage");
            if(a.palette().stream().anyMatch(v->v.contains("deepslate")||v.contains("mangrove")||v.contains("cherry")))throw new AssertionError("Future block in old base "+version);
        }
        for(int era=6;era<20;era++){
            if(era>6&&br.com.mura.betaminecraftmod.progression.EraCatalog.get(era).playable())throw new AssertionError("Base activated unfinished chapter");
            if(era>=13){try{bases.generate(era,0,0,null);throw new AssertionError("Missing modern context silently fell back");}catch(IllegalStateException expected){}}
        }
        if(!ModernEraBiomePolicy.replacement(18,"minecraft:dappled_forest").equals("minecraft:forest")||!ModernEraBiomePolicy.replacement(19,"minecraft:dappled_forest").equals("minecraft:dappled_forest"))throw new AssertionError("Dappled Forest admission boundary");
        if(!ModernEraBiomePolicy.replacement(13,"minecraft:deep_dark").equals("minecraft:dripstone_caves"))throw new AssertionError("Deep dark before 1.19");
        if(!ModernEraBiomePolicy.replacement(14,"minecraft:cherry_grove").equals("minecraft:meadow"))throw new AssertionError("Cherry before 1.20");
        if(!ModernEraBiomePolicy.replacement(16,"minecraft:pale_garden").equals("minecraft:dark_forest"))throw new AssertionError("Pale garden too early");
        System.out.println("Release bases: 11 milestones, seed/order independence, bedrock, height and chapter isolation passed.");
    }
}
