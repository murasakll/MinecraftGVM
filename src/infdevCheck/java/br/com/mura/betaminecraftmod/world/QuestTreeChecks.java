package br.com.mura.betaminecraftmod.world;
import br.com.mura.betaminecraftmod.quests.QuestTree;
import java.util.*;
final class QuestTreeChecks {
    static void run(){
        for(int era=0;era<=6;era++){
            var goals=QuestTree.goals(era);var ids=new HashSet<String>();for(var q:goals)if(!ids.add(q.id()))throw new AssertionError("Duplicate quest");
            for(var q:goals)if(!ids.containsAll(q.requires()))throw new AssertionError("Missing dependency");
            var none=QuestTree.completed(era,f->0,Set.of());if(!none.isEmpty()||QuestTree.ready(era,none))throw new AssertionError("Unopened tree complete");
            var all=QuestTree.completed(era,f->10000,Set.of());if(all.size()!=goals.size()||!QuestTree.ready(era,all))throw new AssertionError("Tree unreachable");
            if(!all.equals(QuestTree.fromMask(era,QuestTree.mask(era,all))))throw new AssertionError("Round trip");
            for(String branch:new String[]{"prep","explore","combat","build"}){var partial=new HashSet<>(all);for(var q:goals)if(q.branch().equals(branch))partial.remove(q.id());if(QuestTree.ready(era,partial))throw new AssertionError("Branch bypass");}
            var required=new HashSet<>(all);for(var q:goals)if(q.optional())required.remove(q.id());if(!QuestTree.ready(era,required))throw new AssertionError("Optional gate");
            if(!QuestTree.completed(era,f->0,all).equals(all))throw new AssertionError("Consuming items revoked a quest");
        }
        if(QuestTree.ready(7,Set.of())||!QuestTree.goals(7).isEmpty())throw new AssertionError("Unimplemented tree unlocked");
        int prior=0;for(int era=0;era<20;era++){var plan=QuestTree.siege(era);int sum=0;for(int w=0;w<plan.waves();w++){int size=plan.waveSize(w);if(size<=0)throw new AssertionError("Empty wave");sum+=size;}if(sum!=plan.total()||sum<=prior||plan.concurrent()>24||plan.concurrent()<1)throw new AssertionError("Siege budget/cap");prior=sum;}
        for(int era=0;era<20;era++){
            var plan=QuestTree.siege(era);
            if(plan.restTicks()<12*20||plan.waves()<2||plan.waves()>8)throw new AssertionError("Unbounded siege duration/rest");
            int smallest=Integer.MAX_VALUE,largest=0;
            for(int wave=0;wave<plan.waves();wave++){int size=plan.waveSize(wave);smallest=Math.min(smallest,size);largest=Math.max(largest,size);}
            if(largest-smallest>1)throw new AssertionError("Unbalanced distribution between waves");
            if(era>0){var previous=QuestTree.siege(era-1);if(plan.waves()<previous.waves()||plan.concurrent()<previous.concurrent()||plan.restTicks()>previous.restTicks())throw new AssertionError("Siege difficulty regressed");}
        }
        System.out.println("QUEST_TREE SUCCESS: seven acyclic catalogs, four mandatory branches, optional exclusion, mask persistence, monotonic completion and 20 growing siege budgets");
    }
}
