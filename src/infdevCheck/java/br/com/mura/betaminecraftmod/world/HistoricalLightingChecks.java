package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.client.HistoricalLighting;
import java.nio.file.*;

final class HistoricalLightingChecks {
    static void run()throws Exception {
        int maps=0,curves=0,skies=0;
        for(String row:Files.readAllLines(Path.of("docs/reference/lightmaps.tsv"))) {
            if(row.startsWith("#")||row.isBlank())continue;var f=row.split(" ");var input=new HistoricalLighting.Inputs(Integer.parseInt(f[0]),Integer.parseInt(f[1]),Float.parseFloat(f[2]),0,Float.parseFloat(f[3]),Float.parseFloat(f[4]),Boolean.parseBoolean(f[5]));
            var bytes=java.nio.ByteBuffer.allocate(1024);for(int color:HistoricalLighting.pixels(input))bytes.putInt(color);
            if(!java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes.array())).equals(f[6]))throw new AssertionError("Historical colored lightmap differs: "+row);maps++;
        }
        for(String row:Files.readAllLines(Path.of("docs/reference/lighting_curves.tsv"))) {
            if(row.startsWith("#")||row.isBlank())continue;var f=row.split(" ");for(int level=0;level<16;level++)if(Float.floatToIntBits(HistoricalLighting.brightness(level,HistoricalLighting.minimum(Integer.parseInt(f[0]),Integer.parseInt(f[1]))))!=(int)Long.parseLong(f[2+level],16))throw new AssertionError("Original light curve changed");curves++;
        }
        for(String row:Files.readAllLines(Path.of("docs/reference/lighting_sky.tsv"))) {
            if(row.startsWith("#")||row.isBlank())continue;var f=row.split(" ");int dim=Integer.parseInt(f[1]);long time=Long.parseLong(f[2]);float partial=Float.parseFloat(f[3]),rain=Float.parseFloat(f[4]),thunder=Float.parseFloat(f[5]);
            if(Float.floatToIntBits(HistoricalLighting.sky(time,partial,dim,rain,thunder,true))!=(int)Long.parseLong(f[6],16)||(int)((1-HistoricalLighting.sky(time,partial,dim,rain,thunder,false))*11)!=Integer.parseInt(f[7]))throw new AssertionError("Original sun/weather changed");skies++;
        }
        if(maps!=270||curves!=9||skies!=360)throw new AssertionError("Incomplete lighting reference fixtures");
        for(int era=0;era<=6;era++){if(!HistoricalLighting.active(era,"1.0")||HistoricalLighting.ambientOcclusion(era)!=(era>=4))throw new AssertionError("Wrong lighting era policy");}
        if(HistoricalLighting.active(-1,"1.0")||HistoricalLighting.active(7,"1.0")||HistoricalLighting.active(6,"1.1")||HistoricalLighting.active(6,"1.2.5"))throw new AssertionError("Historical lighting leaked into unverified release");
        System.out.println("HISTORICAL_LIGHTING SUCCESS: 270 original lightmaps / 69120 pixels, nine dimension curves, 360 sun/weather cases and era isolation");
    }
}
