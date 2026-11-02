package br.com.mura.betaminecraftmod.world;

import java.util.List;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;

/** Reference-checked assembly bridge. Actual world placement is handled separately. */
public final class HistoricalFortressLayout {
    private static final ThreadLocal<Boolean> ORIGINAL=ThreadLocal.withInitial(()->false);
    public static boolean original(){return ORIGINAL.get();}
    public record Layout(List<StructurePiece> pieces,long nextLong) { }
    private HistoricalFortressLayout() { }
    public static synchronized Layout assemble(long randomSeed,int cx,int cz) {
        return assemble(new LegacyRandomSource(randomSeed),cx,cz);
    }
    public static synchronized Layout assemble(RandomSource random,int cx,int cz) {
        boolean previous=ORIGINAL.get();ORIGINAL.set(true);
        try {
            var builder=new StructurePiecesBuilder();
            var start=new NetherFortressPieces.StartPiece(random,cx*16+2,cz*16+2);
            builder.addPiece(start);start.addChildren(start,builder,random);
            while(!start.pendingChildren.isEmpty())start.pendingChildren.remove(random.nextInt(start.pendingChildren.size())).addChildren(start,builder,random);
            builder.moveInsideHeights(random,48,70);
            return new Layout(List.copyOf(builder.build().pieces()),random.nextLong());
        }finally {ORIGINAL.set(previous);}
    }
}
