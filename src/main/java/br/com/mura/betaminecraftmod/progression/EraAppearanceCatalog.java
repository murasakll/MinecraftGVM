package br.com.mura.betaminecraftmod.progression;

import java.util.List;

/** Texture identity is versioned independently from terrain, lighting and sound readiness. */
public final class EraAppearanceCatalog {
    public enum State { ACTIVE_PARTIAL, STAGED_PARTIAL, PENDING, NATIVE }
    public record Profile(int era,String reference,String textureSet,State state,List<String> packs,List<String> checkpoints) {
        public Profile {packs=List.copyOf(packs);checkpoints=List.copyOf(checkpoints);}
    }
    private static final String[] REFERENCES={"Infdev 20100617-1531","Infdev 20100630-1835","Alpha 1.1.2_01","Alpha 1.2.6","Beta 1.7.3","Beta 1.8.1","1.2.5","1.7.10","1.9.4","1.12.2","1.13.2","1.15.2","1.16.5","1.18.2","1.19.4","1.20.6","1.21.3","1.21.8","1.21.11","26.3"};
    private static final List<Profile> ALL=java.util.stream.IntStream.range(0,20).mapToObj(id->new Profile(id,REFERENCES[id],
        "reference_"+id,id<3?State.ACTIVE_PARTIAL:id<=6?State.STAGED_PARTIAL:id==19?State.NATIVE:State.PENDING,
        id==0?List.of("resourcepacks/infdev"):id==1?List.of("resourcepacks/infdev","resourcepacks/infdev_late"):
            id==2?List.of("resourcepacks/infdev","resourcepacks/alpha_early"):id==3?List.of("resourcepacks/visual_halloween","resourcepacks/chest_halloween"):id==4?List.of("resourcepacks/visual_beta","resourcepacks/chest_beta"):id==5?List.of("resourcepacks/visual_adventure"):id==6?List.of("resourcepacks/visual_release_1_0"):List.of(),checkpoints(id))).toList();
    private EraAppearanceCatalog() { }
    public static Profile get(int era){return era>=0&&era<ALL.size()?ALL.get(era):null;}
    public static List<Profile> all(){return ALL;}
    public static List<String> managedTexturePacks(){return ALL.stream().flatMap(p->p.packs().stream()).distinct().toList();}
    private static List<String> checkpoints(int id) {
        return switch(id) {
            case 6->List.of("1.0","1.1","1.2.5");
            case 7->List.of("1.3","1.4","1.5","1.6","1.7.10");
            case 8->List.of("1.8","1.9.4");
            case 9->List.of("1.10","1.11","1.12.2");
            case 11->List.of("1.14 Texture Update","1.15.2");
            case 13->List.of("1.17","1.18.2");
            case 16->List.of("1.21","1.21.3");
            case 17->List.of("Pale Garden","Spring","Skies");
            case 18->List.of("Copper","Mounts");
            default->List.of(REFERENCES[id]);
        };
    }
}
