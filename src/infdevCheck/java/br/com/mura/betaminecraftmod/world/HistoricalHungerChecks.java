package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.HistoricalHunger;

final class HistoricalHungerChecks {
    static void run() {
        var state=new HistoricalHunger.State(20,5,0,0);
        for(int i=1;i<=80;i++) {
            var tick=HistoricalHunger.tick(state,2,10,true,true);state=tick.state();
            if(tick.heal()!=(i==80)||tick.starve())throw new AssertionError("Modern fast regeneration leaked");
        }
        if(state.exhaustion()!=0||state.saturation()!=5)throw new AssertionError("Later regeneration cost leaked into 1.0");
        if(HistoricalHunger.tick(new HistoricalHunger.State(20,0,4,0),2,20,false,true).state().food()!=20)throw new AssertionError("Exhaustion threshold is strict");
        if(HistoricalHunger.tick(new HistoricalHunger.State(20,0,4.01f,0),2,20,false,true).state().food()!=19)throw new AssertionError("Exhaustion did not consume hunger");
        for(int difficulty=0;difficulty<4;difficulty++)for(float health:new float[]{1,2,10,11}) {
            var tick=HistoricalHunger.tick(new HistoricalHunger.State(0,0,0,79),difficulty,health,true,true);
            if(tick.starve()!=(health>10||difficulty>=3||health>1&&difficulty>=2)||tick.state().timer()!=0)throw new AssertionError("Starvation difficulty floor changed");
        }
        if(HistoricalHunger.tick(new HistoricalHunger.State(20,5,0,79),2,10,true,false).heal())throw new AssertionError("Regeneration gamerule ignored");
        System.out.println("HISTORICAL_HUNGER SUCCESS: 80-tick regeneration, original costs, strict exhaustion and starvation floors");
    }
}
