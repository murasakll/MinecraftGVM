package br.com.mura.betaminecraftmod.progression;

/** Registry-free Beta 1.8.1/release 1.0 food tick policy. */
public final class HistoricalHunger {
    public record State(int food,float saturation,float exhaustion,int timer) { }
    public record Tick(State state,boolean heal,boolean starve) { }
    public static Tick tick(State previous,int difficulty,float health,boolean hurt,boolean naturalRegeneration) {
        int food=previous.food(),timer=previous.timer();float saturation=previous.saturation(),exhaustion=previous.exhaustion();
        if(exhaustion>4) {
            exhaustion-=4;
            if(saturation>0)saturation=Math.max(0,saturation-1);
            else if(difficulty>0)food=Math.max(0,food-1);
        }
        boolean heal=false,starve=false;
        if(food>=18&&hurt&&naturalRegeneration) {
            if(++timer>=80){heal=true;timer=0;}
        }else if(food<=0) {
            if(++timer>=80){starve=health>10||difficulty>=3||health>1&&difficulty>=2;timer=0;}
        }else timer=0;
        return new Tick(new State(food,saturation,exhaustion,timer),heal,starve);
    }
    private HistoricalHunger() { }
}
