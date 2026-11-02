package br.com.mura.betaminecraftmod.client;

import br.com.mura.betaminecraftmod.world.HalloweenClimate;
import br.com.mura.betaminecraftmod.world.AdventureBiomes;
import br.com.mura.betaminecraftmod.world.HistoricalVegetationPalette;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Immutable session publication; compilation workers never access Minecraft or the integrated server. */
public final class HistoricalVegetationColors {
    private record Session(int era,long seed,int dimension,HalloweenClimate climate,AdventureBiomes layered,boolean verified) { }
    private record Tile(Session session,int x,int z,HalloweenClimate.Grid grid) { }
    private record LayerTile(Session session,int x,int z,AdventureBiomes.ClimateGrid climate,int[] biomes) { }
    private static volatile Session session=new Session(-1,0,0,null,null,false);
    private static final ThreadLocal<Tile> tile=new ThreadLocal<>();
    private static final ThreadLocal<LayerTile> layerTile=new ThreadLocal<>();
    private static final class Palettes {
        static final int[][] MAPS={read("halloween","grass"),read("halloween","foliage"),read("beta","grass"),read("beta","foliage"),read("adventure","grass"),read("adventure","foliage"),read("release_1_0","grass"),read("release_1_0","foliage")};
        private static int[] read(String era,String type) {
            String path="/assets/betaminecraftmod/textures/colormap/"+era+"_"+type+".png";
            try(var input=HistoricalVegetationColors.class.getResourceAsStream(path)) {
                if(input==null)throw new IllegalStateException("Missing historical colormap "+path);
                try(var image=NativeImage.read(input)) {
                    if(image.getWidth()!=256||image.getHeight()!=256)throw new IllegalStateException("Invalid colormap dimensions");
                    int[] pixels=new int[65536];for(int z=0;z<256;z++)for(int x=0;x<256;x++)pixels[z*256+x]=image.getPixel(x,z);
                    return pixels;
                }
            }catch(java.io.IOException error){throw new IllegalStateException("Could not read "+path,error);}
        }
    }
    public static void configure(long seed,boolean nether){configure(seed,nether?-1:0);}
    public static void configure(long seed,int dimension) {var old=session;session=new Session(old.era,seed,dimension,new HalloweenClimate(seed),old.era>=5&&old.era<=6?new AdventureBiomes(seed,old.era==6):null,old.verified);}
    public static void select(int era) {
        var old=session;
        boolean verified=era>=3&&era<=6&&(era!=6||br.com.mura.betaminecraftmod.progression.CampaignRules.firstRelease((net.minecraft.world.level.Level)null).equals("1.0"));
        session=era<0?new Session(-1,0,0,null,null,false):new Session(era,old.seed,old.dimension,old.climate,era>=5&&era<=6?new AdventureBiomes(old.seed,era==6):null,verified);
    }
    public static boolean active(){return active(session);}
    private static boolean active(Session current){return current.climate!=null&&current.verified;}
    public static int color(BlockState state,BlockPos pos) {
        return color(session,state,pos);
    }
    private static int color(Session current,BlockState state,BlockPos pos) {
        if(!active(current))throw new IllegalStateException("Historical vegetation is inactive");
        boolean foliage=!state.is(Blocks.GRASS_BLOCK)&&!state.is(Blocks.SHORT_GRASS)&&!state.is(Blocks.FERN);
        if(current.era>=4&&state.is(Blocks.SPRUCE_LEAVES))return 0xFF619961;
        if(current.era>=4&&state.is(Blocks.BIRCH_LEAVES))return 0xFF80A755;
        double heat=current.dimension==1?.5:1,wet=0;boolean swamp=false;
        if(current.dimension==0) {
            int px=pos.getX(),pz=pos.getZ();
            if((current.era==4||current.era==5)&&(state.is(Blocks.SHORT_GRASS)||state.is(Blocks.FERN))) {
                long scatter=px*3129871+pz*6129781+pos.getY();scatter=scatter*scatter*42317861L+scatter*11L;
                int offset=current.era==5?16:0;px+=(int)(scatter>>14&31L)-offset;pz+=(int)(scatter>>24&31L)-offset;
            }
            int x=Math.floorDiv(px,16),z=Math.floorDiv(pz,16);
            if(current.era<=4){
                var cached=tile.get();if(cached==null||cached.session!=current||cached.x!=x||cached.z!=z){cached=new Tile(current,x,z,current.climate.sample(x*16,z*16,16,16));tile.set(cached);}
                int index=(px&15)*16+(pz&15);heat=cached.grid.temperature()[index];wet=cached.grid.rainfall()[index];
            }else {
                var cached=layerTile.get();if(cached==null||cached.session!=current||cached.x!=x||cached.z!=z){cached=new LayerTile(current,x,z,current.layered.climate(x*16,z*16,16,16),current.era==6?current.layered.blocks(x*16,z*16,16,16):null);layerTile.set(cached);}
                int index=(px&15)+(pz&15)*16;heat=Math.min(cached.climate.temperature()[index]/65536F,1F);wet=Math.min(cached.climate.rainfall()[index]/65536F,1F);swamp=current.era==6&&cached.biomes[index]==6;
            }
        }
        int color=HistoricalVegetationPalette.color(Palettes.MAPS[(current.era-3)*2+(foliage?1:0)],heat,wet);
        return swamp?(((color&0xFEFEFE)+5115470)/2)|0xFF000000:color;
    }
    public static BlockTintSource wrap(BlockTintSource nativeSource) {
        return new BlockTintSource() {
            @Override public int color(BlockState state){return nativeSource.color(state);}
            @Override public int colorInWorld(BlockState state,BlockAndTintGetter level,BlockPos pos){var current=session;return active(current)?HistoricalVegetationColors.color(current,state,pos):nativeSource.colorInWorld(state,level,pos);}
            @Override public int colorAsTerrainParticle(BlockState state,BlockAndTintGetter level,BlockPos pos){var current=session;return active(current)&&!state.is(Blocks.GRASS_BLOCK)?HistoricalVegetationColors.color(current,state,pos):nativeSource.colorAsTerrainParticle(state,level,pos);}
            @Override public java.util.Set<net.minecraft.world.level.block.state.properties.Property<?>> relevantProperties(){return nativeSource.relevantProperties();}
        };
    }
    private HistoricalVegetationColors() { }
}
