package br.com.mura.betaminecraftmod.client;

/** Conservative radial fog: it becomes opaque before any side of the unlocked square. */
public final class BoundaryFog {
    private BoundaryFog() { }
    public static float end(float ordinaryEnd,double x,double z,double minX,double maxX,double minZ,double maxZ) {
        double edge=Math.min(Math.min(x-minX,maxX-x),Math.min(z-minZ,maxZ-z));
        return Math.min(ordinaryEnd,(float)Math.max(.25,edge-4));
    }
}
