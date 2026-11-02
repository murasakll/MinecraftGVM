package br.com.mura.betaminecraftmod.progression;

/** Resolves an outstanding death before the host replaces its player object. */
public interface HistoricalDeathExperienceAccess {
    void campaign$resolveDeathExperience();
}
