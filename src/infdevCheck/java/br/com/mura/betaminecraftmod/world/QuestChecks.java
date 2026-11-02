package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.quests.QuestProgress;
import br.com.mura.betaminecraftmod.quests.QuestChapter;

public final class QuestChecks {
    public static void run() {
        QuestProgress state = QuestProgress.EMPTY;
        if (state.claim(0).stage() != 0) throw new AssertionError("Unfinished intro claimed");
        state = state.open().claim(0);
        if (state.stage() != 1 || state.claim(0).stage() != 1) throw new AssertionError("Stale stage replay accepted");
        if (state.wood(7).claim(1).stage() != 1) throw new AssertionError("Insufficient materials accepted");
        state = state.wood(8).claim(1);
        if (state.stage() != 2 || state.claim(2).stage() != 2) throw new AssertionError("Tool objective skipped");
        state = state.crafted().claim(2);
        for (int i = 0; i < 12; i++) state = state.place();
        if (state.claim(3).stage() != 3) throw new AssertionError("Blocks alone do not prove shelter");
        state = state.shelter().claim(3);
        if (state.stage() != 4 || state.ready() || state.claim(3).stage() != 4 || state.claim(4).stage() != 4) throw new AssertionError("Completed campaign rewarded twice");
        if (new QuestProgress(100, -5, false, -9, false, false).stage() != 4) throw new AssertionError("Bad state not bounded");
        var late = QuestChapter.LATE;
        var next = QuestProgress.EMPTY.open();
        if (next.claim(0, 0, late) != next) throw new AssertionError("Packet from previous chapter accepted");
        next = next.claim(1, 0, late);
        if (next.wood(31).claim(1, 1, late).stage() != 1) throw new AssertionError("Late materials too easy");
        next = next.wood(32).claim(1, 1, late).crafted().claim(1, 2, late);
        for (int i = 0; i < 47; i++) next = next.place();
        if (next.shelter().claim(1, 3, late).stage() != 3) throw new AssertionError("Late shelter too small");
        next = next.place().shelter().claim(1, 3, late);
        if (next.stage() != 4 || next.claim(1, 3, late) != next) throw new AssertionError("Late reward replay accepted");
        if (QuestChapter.get(7) != null || QuestChapter.get(3) == null || !QuestChapter.FIRST.storagePrefix().equals("betaminecraftmod_q_")
                || late.storagePrefix().equals(QuestChapter.FIRST.storagePrefix())) throw new AssertionError("Chapter storage migration broken");
        var alpha = QuestChapter.ALPHA;
        var circuit = QuestProgress.EMPTY.open();
        if (circuit.claim(1,0,alpha) != circuit) throw new AssertionError("Previous-era packet accepted by Alpha");
        circuit = circuit.claim(2,0,alpha);
        if (circuit.wood(15).claim(2,1,alpha).stage() != 1) throw new AssertionError("Alpha iron threshold skipped");
        circuit = circuit.wood(16).claim(2,1,alpha).crafted().claim(2,2,alpha);
        for(int i=0;i<16;i++) circuit=circuit.place();
        if (circuit.claim(2,3,alpha).stage()!=3) throw new AssertionError("Unpowered circuit accepted");
        circuit=circuit.shelter().claim(2,3,alpha);
        if(circuit.stage()!=4 || circuit.claim(2,3,alpha)!=circuit) throw new AssertionError("Alpha duplicate reward");
        System.out.println("Quests: prerequisites, stale claims, shelter and duplicate reward checks passed.");
    }
}
