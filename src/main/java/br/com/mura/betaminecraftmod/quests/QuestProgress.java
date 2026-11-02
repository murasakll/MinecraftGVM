package br.com.mura.betaminecraftmod.quests;

/** Immutable chapter state. Legacy field wood stores the chapter material counter. */
public record QuestProgress(int stage, int wood, boolean tool, int placed, boolean sheltered, boolean opened) {
    public QuestProgress {
        stage = Math.clamp(stage, 0, 4); wood = Math.clamp(wood, 0, 4096); placed = Math.clamp(placed, 0, 4096);
    }
    public static final QuestProgress EMPTY = new QuestProgress(0, 0, false, 0, false, false);
    public QuestProgress withStage(int value) { return new QuestProgress(value, wood, tool, placed, sheltered, opened); }
    public QuestProgress claim(int expectedEra, int expectedStage, QuestChapter chapter) {
        return chapter != null && chapter.accepts(expectedEra, expectedStage, this) && chapter.ready(this) ? withStage(stage + 1) : this;
    }
    public boolean ready() {
        return switch (stage) { case 0 -> opened; case 1 -> wood >= 8; case 2 -> tool; case 3 -> placed >= 12 && sheltered; default -> false; };
    }
    public QuestProgress claim(int expectedStage) { return stage == expectedStage && ready() ? new QuestProgress(stage + 1, wood, tool, placed, sheltered, opened) : this; }
    public QuestProgress open() { return new QuestProgress(stage, wood, tool, placed, sheltered, true); }
    public QuestProgress wood(int amount) { return new QuestProgress(stage, Math.max(wood, amount), tool, placed, sheltered, opened); }
    public QuestProgress crafted() { return new QuestProgress(stage, wood, true, placed, sheltered, opened); }
    public QuestProgress place() { return new QuestProgress(stage, wood, tool, placed + 1, sheltered, opened); }
    public QuestProgress shelter() { return new QuestProgress(stage, wood, tool, placed, true, opened); }
    public String title() { return switch (stage) {
        case 0 -> "Um mundo para descobrir"; case 1 -> "Primeiros materiais"; case 2 -> "Seu primeiro instrumento";
        case 3 -> "Um lugar seu"; default -> "Primeiros passos conclu\u00eddos";
    }; }
    public String description() { return switch (stage) {
        case 0 -> "Este painel acompanha sua jornada. Abra os objetivos e confirme para conhecer o pr\u00f3ximo passo.";
        case 1 -> "Junte 8 troncos de carvalho no invent\u00e1rio. Voc\u00ea pode explorar com calma.";
        case 2 -> "Fabrique sua primeira ferramenta de madeira: picareta, machado, p\u00e1 ou enxada.";
        case 3 -> "Prepare um abrigo. Coloque 12 blocos, tenha um teto acima de voc\u00ea e prote\u00e7\u00e3o em pelo menos tr\u00eas lados.";
        default -> "Voc\u00ea conheceu os objetivos, reuniu materiais e preparou seu primeiro abrigo.";
    }; }
    public String counter() { return switch (stage) {
        case 0 -> opened ? "Painel conhecido" : "Abra o painel";
        case 1 -> wood + " / 8 troncos"; case 2 -> tool ? "Ferramenta fabricada" : "0 / 1 ferramenta";
        case 3 -> placed + " / 12 blocos | " + (sheltered ? "Abrigo pronto" : "Procure o interior do seu abrigo"); default -> "4 / 4 objetivos";
    }; }
    public String reward() { return switch (stage) {
        case 0 -> "Pr\u00f3ximo objetivo"; case 1 -> "4 gravetos"; case 2 -> "4 tochas"; case 3 -> "4 p\u00e3es"; default -> "";
    }; }
}
