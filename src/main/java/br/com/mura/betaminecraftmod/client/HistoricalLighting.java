package br.com.mura.betaminecraftmod.client;

/** Pure numerical light policy. Geometry, sky rendering and fog remain independently versioned. */
public final class HistoricalLighting {
    public record Inputs(int era,int dimension,float sun,int skyDarkening,float flicker,float gamma,boolean lightning) { }
    private static final float[] SIN=new float[65536];
    static {for(int i=0;i<SIN.length;i++)SIN[i]=(float)Math.sin(i*Math.PI*2/65536);}
    private HistoricalLighting() { }
    public static boolean active(int era,String firstRelease){return era>=0&&era<=6&&(era!=6||firstRelease.equals("1.0"));}
    public static boolean ambientOcclusion(int era){return era<0||era>=4;}
    public static float brightness(int level,float minimum){float dark=1-Math.clamp(level,0,15)/15F;return (1-dark)/(dark*3+1)*(1-minimum)+minimum;}
    public static float minimum(int era,int dimension){return dimension==-1?.1F:era<=4?.05F:0;}
    public static float angle(long time,float partial,int dimension){
        if(dimension==-1)return .5F;if(dimension==1)return 0;
        float phase=(Math.floorMod(time,24000)+partial)/24000F-.25F;if(phase<0)phase++;if(phase>1)phase--;
        float smooth=1-(float)((Math.cos(phase*Math.PI)+1)/2);return phase+(smooth-phase)/3;
    }
    private static float cosine(float angle){return SIN[(int)(angle*10430.378F+16384F)&65535];}
    public static float sky(long time,float partial,int dimension,float rain,float thunder,boolean colored) {
        float value=1-Math.clamp(1-(cosine(angle(time,partial,dimension)*(float)Math.PI*2)*2+(colored?.2F:.5F)),0,1);
        value=(float)(value*(1.0-rain*5F/16.0));value=(float)(value*(1.0-thunder*5F/16.0));return colored?value*.8F+.2F:value;
    }
    public static int[] pixels(Inputs input){var pixels=new int[256];fill(input,pixels);return pixels;}
    public static void fill(Inputs input,int[] pixels) {
        if(pixels.length!=256)throw new IllegalArgumentException("Lightmap must contain 256 pixels");
        float minimum=minimum(input.era,input.dimension);
        for(int sky=0;sky<16;sky++)for(int block=0;block<16;block++) {
            int index=block+sky*16;
            if(input.era<=4){int effective=Math.max(block,Math.max(0,sky-(input.lightning?0:input.skyDarkening)));int gray=Math.round(brightness(effective,minimum)*255);pixels[index]=0xFF000000|gray<<16|gray<<8|gray;continue;}
            float skyLight=brightness(sky,minimum)*(input.sun*.95F+.05F);
            if(input.lightning)skyLight=brightness(sky,minimum);
            float torch=brightness(block,minimum)*(input.flicker*.1F+1.5F);
            float warm=skyLight*(input.sun*.65F+.35F);
            float red=torch,green=torch*((torch*.6F+.4F)*.6F+.4F),blue=torch*(torch*torch*.6F+.4F);
            if(input.era==6&&input.dimension==1){red=.22F+red*.75F;green=.28F+green*.75F;blue=.25F+blue*.75F;}
            else {red=(warm+red)*.96F+.03F;green=(warm+green)*.96F+.03F;blue=(skyLight+blue)*.96F+.03F;}
            pixels[index]=0xFF000000|channel(red,input.gamma)<<16|channel(green,input.gamma)<<8|channel(blue,input.gamma);
        }
    }
    private static int channel(float value,float gamma) {
        value=Math.min(value,1);float inverse=1-value;float corrected=1-inverse*inverse*inverse*inverse;
        value=value*(1-gamma)+corrected*gamma;value=value*.96F+.03F;return (int)(Math.clamp(value,0,1)*255);
    }
}
