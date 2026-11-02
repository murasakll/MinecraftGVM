package br.com.mura.betaminecraftmod.progression;

/** Extra attack impulse, separate from the target's normal hurt reaction. */
public final class HistoricalExtraKnockback {
    private static final float[] SIN=new float[65536];
    static { for(int i=0;i<SIN.length;i++)SIN[i]=(float)Math.sin(i*Math.PI*2.0/65536.0); }
    private HistoricalExtraKnockback(){}
    public record Impulse(double x,double y,double z,boolean applied){}
    public static Impulse impulse(int era,float yaw,boolean sprint,int enchant) {
        int strength=era==5?(sprint?1:0):era==6?enchant+(sprint?1:0):0;
        if(strength<=0)return new Impulse(0,0,0,false);
        float angle=yaw*(float)Math.PI/180F;
        float sin=SIN[(int)(angle*10430.378F)&65535];
        float cos=SIN[(int)(angle*10430.378F+16384F)&65535];
        float x=-sin*strength,z=cos*strength;
        if(era==6){x*=.5F;z*=.5F;}
        return new Impulse(x,.1,z,true);
    }
}
