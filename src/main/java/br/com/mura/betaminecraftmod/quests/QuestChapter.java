package br.com.mura.betaminecraftmod.quests;

/** Chapter-specific goals. These definitions do not enable an unfinished terrain profile. */
public record QuestChapter(int era, String path, String name, int materials, int blocks) {
    public static final QuestChapter FIRST = new QuestChapter(0, "infdev", "Infdev 17/06/2010", 8, 12);
    public static final QuestChapter LATE = new QuestChapter(1, "infdev_late", "Infdev 30/06/2010", 32, 48);

    public static final QuestChapter ALPHA = new QuestChapter(2, "alpha_early", "Alpha 1.1.2_01", 16, 16);

    private static final java.util.List<QuestChapter> LATER=java.util.List.of(
        new QuestChapter(3,"halloween","Halloween / Alpha 1.2",24,64),
        new QuestChapter(4,"beta","Beta até 1.7.3",32,80),
        new QuestChapter(5,"adventure","Adventure Update",32,96),
        new QuestChapter(6,"release_1_0","Lançamento 1.0",32,128));
    public static QuestChapter get(int era) { return switch (era) { case 0 -> FIRST; case 1 -> LATE; case 2 -> ALPHA; default -> era>=3&&era<=6?LATER.get(era-3):null; }; }
    public String storagePrefix() { return era == 0 ? "betaminecraftmod_q_" : "betaminecraftmod_q_era_" + era + "_"; }
    public boolean accepts(int requestEra, int expectedStage, QuestProgress state) {
        return requestEra == era && expectedStage == state.stage();
    }
    public boolean ready(QuestProgress state) {
        return switch (state.stage()) {
            case 0 -> state.opened(); case 1 -> state.wood() >= materials;
            case 2 -> state.tool(); case 3 -> state.placed() >= blocks && state.sheltered(); default -> false;
        };
    }
    public String counter(QuestProgress state) {
        return switch (state.stage()) {
            case 0 -> state.opened() ? "Painel conhecido" : "Abra o painel";
            case 1 -> state.wood() + " / " + materials + (era == 0 ? " troncos" : era == 1 ? " pedregulhos" : " barras de ferro");
            case 2 -> state.tool() ? "Ferramenta fabricada" : "0 / 1 ferramenta";
            case 3 -> state.placed() + " / " + blocks + (era == 2 ? " fios | " + (state.sheltered() ? "Circuito ativado" : "Ative o circuito perto de voce") : " blocos | " + (state.sheltered() ? "Abrigo pronto" : "Entre no abrigo"));
            default -> "4 / 4 objetivos";
        };
    }
    public String reward(int stage) {
        if (era != 2) return QuestProgress.EMPTY.withStage(stage).reward();
        return switch(stage) { case 0 -> "Proximo objetivo"; case 1 -> "8 carvoes"; case 2 -> "16 tochas"; case 3 -> "8 paes"; default -> ""; };
    }
    public String description(int stage) {
        if (era == 2) return switch (stage) {
            case 0 -> "A era das primeiras invencoes: prepare ferro e experimente a redstone.";
            case 1 -> "Reuna 16 barras de ferro no inventario.";
            case 2 -> "Fabrique uma picareta de ferro nesta etapa para minerar redstone.";
            case 3 -> "Coloque 16 fios de redstone e ative um fio a ate 5 blocos de voce com uma alavanca ou tocha de redstone.";
            default -> "Suas primeiras invencoes estao prontas. Continue na Alpha; o Halloween e o proximo capitulo.";
        };
        if (era == 0) return QuestProgress.EMPTY.withStage(stage).description();
        return switch (stage) {
            case 0 -> "Uma nova etapa da Infdev. Prepare recursos e amplie sua base no mesmo mundo.";
            case 1 -> "Re\u00fana 32 pedregulhos no invent\u00e1rio para ampliar sua base.";
            case 2 -> "Fabrique uma picareta de pedra nesta etapa.";
            case 3 -> "Coloque 48 blocos nesta etapa e entre em um abrigo com teto e tr\u00eas lados protegidos.";
            default -> "Sua base est\u00e1 preparada. Continue nesta era ou avance quando a pr\u00f3xima estiver dispon\u00edvel.";
        };
    }
}
