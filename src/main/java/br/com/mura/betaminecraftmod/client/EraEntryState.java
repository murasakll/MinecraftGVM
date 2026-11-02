package br.com.mura.betaminecraftmod.client;

/** The integrated server's internal disconnect must not discard a prepared resource selection. */
public final class EraEntryState {
    private enum Phase { IDLE, PREPARING, JOINING }
    private Phase phase=Phase.IDLE;
    public boolean begin() { if(phase!=Phase.IDLE) return false; phase=Phase.PREPARING; return true; }
    public void joining() { phase=Phase.JOINING; }
    public void clear() { phase=Phase.IDLE; }
    public boolean inProgress() { return phase!=Phase.IDLE; }
}
