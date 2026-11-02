package br.com.mura.betaminecraftmod.world;

/** Climate lookup shared by Alpha/Beta; coordinates are sampled without modern biome blending. */
public final class HistoricalVegetationPalette {
    private HistoricalVegetationPalette() { }
    public static int index(double temperature,double rainfall) {
        double heat=Math.clamp(temperature,0,1),wet=Math.clamp(rainfall,0,1)*heat;
        return ((int)((1-wet)*255)<<8)|(int)((1-heat)*255);
    }
    public static int color(int[] pixels,double temperature,double rainfall) {
        if(pixels.length!=65536)throw new IllegalArgumentException("Historical colormap must be 256 by 256");
        return pixels[index(temperature,rainfall)]|0xFF000000;
    }
}
