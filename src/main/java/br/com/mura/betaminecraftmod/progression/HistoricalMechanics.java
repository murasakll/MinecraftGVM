package br.com.mura.betaminecraftmod.progression;

import java.util.List;

/** Per-era boundaries; a native fallback is explicit, not a historical certification. */
public final class HistoricalMechanics {
    public enum Furnace { INFDEV, ALPHA, HALLOWEEN, BETA, ADVENTURE, RELEASE_ONE, NATIVE_PENDING_AUDIT }
    public record Profile(int era,boolean instantBow,Furnace furnace) {
        public boolean historicalFurnace(){return furnace!=Furnace.NATIVE_PENDING_AUDIT;}
    }
    private static final List<Profile> ALL=java.util.stream.IntStream.range(0,20).mapToObj(id->
        new Profile(id,id<5,id<2?Furnace.INFDEV:id==2?Furnace.ALPHA:id==3?Furnace.HALLOWEEN:id==4?Furnace.BETA:id==5?Furnace.ADVENTURE:id==6?Furnace.RELEASE_ONE:Furnace.NATIVE_PENDING_AUDIT)).toList();
    public static Profile get(int era){return era<0||era>=ALL.size()?null:ALL.get(era);}
    public static List<Profile> all(){return ALL;}
    private HistoricalMechanics(){}
}
