package br.com.mura.betaminecraftmod.world;

/** Shared dependency/save gate for campaign dimensions. */
public interface CampaignGenerationGate {
    boolean locked(int chunkX,int chunkZ);
    void unlockThrough(int era);
}
