package br.com.mura.betaminecraftmod.world;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.version.MCVersion;
import java.util.*;

/** Candidate configured-carver integration for 1.13--1.16; 1.17 is a separate family. */
final class AquaticCarvers {
    private static final Set<String> REPLACEABLE=Set.of("stone","granite","diorite","andesite",
        "grass_block","dirt","coarse_dirt","podzol","sandstone","red_sandstone","mycelium","snow","packed_ice","terracotta");
    private final int cx,cz;
    private final OverworldBiomeSource biomes;
    private final Buffer blocks;
    private AquaticCarvers(TerrainVolume input,long seed,MCVersion version,int cx,int cz){
        this.cx=cx;this.cz=cz;biomes=new OverworldBiomeSource(version,seed);blocks=new Buffer(input);
    }
    static TerrainVolume carve(TerrainVolume input,long seed,MCVersion version,int cx,int cz){
        if(input.minY()!=0||input.height()!=256)throw new IllegalArgumentException("Configured carvers need 256-high terrain");
        var carver=new AquaticCarvers(input,seed,version,cx,cz);
        boolean ocean=carver.biomes.getBiomeForNoiseGen(cx*4,0,cz*4).getCategory()==Biome.Category.OCEAN;
        // Carver indices restart for each generation step. The destination biome selects the list.
        carver.step(seed,false,ocean?.06666667f:.14285715f,.02f);
        if(ocean)carver.step(seed,true,.02f,.06666667f);
        return carver.blocks.finish();
    }
    private final BitSet airMask=new BitSet(65536),liquidMask=new BitSet(65536);
    private void step(long seed,boolean underwater,float firstProbability,float secondProbability){
        var paths=new AquaticCarverPaths(cx,cz,(path,x,y,z,radius,vertical,weights)->{
            var random=new Random(path+cx+cz);boolean[] touched=new boolean[256];
            AquaticCarverMask.carve(cx,cz,x,y,z,radius,vertical,weights,underwater?liquidMask:airMask,
                underwater?null:(bx,by,bz)->blocks.get(bx,by,bz).equals("minecraft:water"),underwater?62:255,
                (bx,by,bz)->{
                    String material=blocks.get(bx,by,bz);
                    if(underwater){
                        if(!replaceable(material,true,blocks.get(bx,by+1,bz)))return;
                        blocks.set(bx,by,bz,by<10?"minecraft:lava":by==10?(random.nextFloat()<.25f?"minecraft:magma_block":"minecraft:obsidian"):"minecraft:water");
                    }else{
                        int column=bx*16+bz;
                        if(material.equals("minecraft:grass_block")||material.equals("minecraft:mycelium"))touched[column]=true;
                        if(!replaceable(material,false,blocks.get(bx,by+1,bz)))return;
                        blocks.set(bx,by,bz,by<11?"minecraft:lava":"minecraft:cave_air");
                        if(by>=11&&touched[column]&&blocks.get(bx,by-1,bz).equals("minecraft:dirt")){
                            String top=biomes.getBiome(cx*16+bx,by,cz*16+bz).getSurfaceConfig().getTopBlock().getRegistryName();
                            blocks.set(bx,by-1,bz,top.contains(":")?top:"minecraft:"+top);
                        }
                    }
                });
        });
        long[] a=new long[2],b=new long[2];var r=new Random();
        for(int index=0;index<2;index++){r.setSeed(seed+index);a[index]=r.nextLong();b[index]=r.nextLong();}
        for(int x=cx-8;x<=cx+8;x++)for(int z=cz-8;z<=cz+8;z++)for(int index=0;index<2;index++){
            long salt=seed+index;r.setSeed(x*a[index]^z*b[index]^salt);
            if(r.nextFloat()>(index==0?firstProbability:secondProbability))continue;
            boolean canyon=underwater?index==0:index==1;
            if(canyon)paths.canyon(r,x,z);else paths.caves(r,x,z);
        }
    }
    static boolean replaceable(String name,boolean underwater,String above){
        if(!name.startsWith("minecraft:"))return false;
        String path=name.substring(10);
        if(REPLACEABLE.contains(path)||path.endsWith("_terracotta")&&!path.contains("glazed"))return true;
        if(underwater)return Set.of("sand","gravel","water","lava","obsidian","air","cave_air").contains(path);
        return (path.equals("sand")||path.equals("gravel"))&&!above.equals("minecraft:water");
    }
    private static final class Buffer {
        private final List<String> palette;
        private final Map<String,Short> ids=new HashMap<>();
        private final short[] cells=new short[65536];
        Buffer(TerrainVolume input){
            palette=new ArrayList<>(input.palette());for(int i=0;i<palette.size();i++)ids.putIfAbsent(palette.get(i),(short)i);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<256;y++)cells[(x*16+z)*256+y]=ids.get(input.block(x,y,z));
        }
        String get(int x,int y,int z){return y<0||y>=256?"minecraft:air":palette.get(Short.toUnsignedInt(cells[(x*16+z)*256+y]));}
        void set(int x,int y,int z,String name){
            Short id=ids.get(name);if(id==null){id=(short)palette.size();ids.put(name,id);palette.add(name);}cells[(x*16+z)*256+y]=id;
        }
        TerrainVolume finish(){return new TerrainVolume(0,256,palette,cells);}
    }
}
