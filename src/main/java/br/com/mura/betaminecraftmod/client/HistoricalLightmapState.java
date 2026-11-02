package br.com.mura.betaminecraftmod.client;

/** Pixels belong to the extracted frame, so rendering never rereads the live world. */
public interface HistoricalLightmapState {
    int[] betaminecraftmod$pixels();
    void betaminecraftmod$pixels(int[] pixels);
}
