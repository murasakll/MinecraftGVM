package br.com.mura.betaminecraftmod.progression;

/** Positive orb-grant arithmetic, independently checked against Beta 1.8.1 and release 1.0. */
public final class HistoricalExperienceRules {
    public record State(int level,int points,float progress,int total,int score){}
    private HistoricalExperienceRules(){}
    public static boolean audited(int era,String firstRelease){return era==5||era==6&&"1.0".equals(firstRelease);}
    public static int needed(int era,int level){return era==5?(level+1)*10:7+(level*7>>1);}
    public static State gain(int era,State before,int amount) {
        int level=before.level(),points=before.points(),total=before.total()+amount,score=before.score();float progress=before.progress();
        if(era==5) {
            points+=amount;
            while(points>=needed(5,level)){points-=needed(5,level);level++;}
            progress=points/(float)needed(5,level);
        } else {
            score+=amount;progress+=(float)amount/needed(6,level);
            while(progress>=1F){progress--;level++;}
        }
        return new State(level,points,progress,total,score);
    }
}
