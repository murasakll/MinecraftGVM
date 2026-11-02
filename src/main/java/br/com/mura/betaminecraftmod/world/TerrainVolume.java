package br.com.mura.betaminecraftmod.world;

import java.util.*;

/** Registry-free terrain result; layout is X, Z, Y and palette entries are block identifiers. */
public final class TerrainVolume {
    private final int minY, height;
    private final List<String> palette;
    private final short[] blocks;
    public TerrainVolume(int minY, int height, List<String> palette, short[] blocks) {
        if (height <= 0 || height > 384 || blocks.length != 256 * height || palette.isEmpty())
            throw new IllegalArgumentException("Invalid terrain volume");
        this.minY=minY; this.height=height; this.palette=List.copyOf(palette); this.blocks=blocks.clone();
        for(short block:blocks) if(Short.toUnsignedInt(block)>=palette.size()) throw new IllegalArgumentException("Unknown palette entry");
    }
    public int minY(){return minY;}
    public int height(){return height;}
    public List<String> palette(){return palette;}
    public String block(int x,int y,int z){
        if(x<0||x>=16||z<0||z>=16)throw new IndexOutOfBoundsException();
        return y<minY||y>=minY+height?"minecraft:air":palette.get(Short.toUnsignedInt(blocks[(x*16+z)*height+y-minY]));
    }
    public static TerrainVolume legacy(byte[] input,int height) {
        String[] ids=new String[256];Arrays.fill(ids,"minecraft:air");
        ids[1]="minecraft:stone";ids[2]="minecraft:grass_block";ids[3]="minecraft:dirt";ids[7]="minecraft:bedrock";
        ids[8]=ids[9]="minecraft:water";ids[10]=ids[11]="minecraft:lava";ids[12]="minecraft:sand";ids[13]="minecraft:gravel";
        ids[24]="minecraft:sandstone";ids[79]="minecraft:ice";ids[80]="minecraft:snow_block";ids[110]="minecraft:mycelium";
        short[] blocks=new short[256*height];
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++)blocks[(x*16+z)*height+y]=(short)Byte.toUnsignedInt(input[InfdevTerrain.index(x,y,z)]);
        return new TerrainVolume(0,height,Arrays.asList(ids),blocks);
    }
}
