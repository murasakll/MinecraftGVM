package br.com.mura.betaminecraftmod.client;

/** Numerical reference: Infdev 20100617-1531. No client classes: usable in checks. */
public record EraVisualProfile(boolean historical, boolean ambientOcclusion, float minimumLight,
        int skyColor, int fogColor, float fogStartFraction, float maximumViewDistance,
        float waterDensity, float lavaDensity) {
    public static final EraVisualProfile MODERN = new EraVisualProfile(false, true, 0, 0, 0, 0, 0, 0, 0);
    public static final EraVisualProfile INFDEV = new EraVisualProfile(true, false, .05f,
            10079487, 11587839, .25f, 256, .1f, 2);
    public static EraVisualProfile forEra(int era) {
        var profile = br.com.mura.betaminecraftmod.progression.EraCatalog.get(era);
        return profile != null && profile.visual() == br.com.mura.betaminecraftmod.progression.EraCatalog.Visual.INFDEV ? INFDEV : MODERN;
    }
    public float brightness(int level) {
        float darkness = 1 - Math.clamp(level, 0, 15) / 15f;
        return (1 - darkness) / (darkness * 3 + 1) * (1 - minimumLight) + minimumLight;
    }
    public float sunAngle(long time, float partial) {
        float phase = ((Math.floorMod(time, 24000) + partial) / 24000f - .25f + 1) % 1;
        return phase + (1 - (float)((Math.cos(phase * Math.PI) + 1) / 2) - phase) / 3;
    }
    public float daylight(long time, float partial) {
        return Math.clamp((float)Math.cos(sunAngle(time, partial) * Math.PI * 2) * 2 + .5f, 0, 1);
    }
    public int skyDarkening(long time, float partial) { return (int)((1 - daylight(time, partial)) * 11); }
    /** Modern view slider stays continuous; historical maximum was 256 blocks. */
    public float viewDistance(int chunks) { return Math.clamp(chunks * 16f, 32, maximumViewDistance); }
}
