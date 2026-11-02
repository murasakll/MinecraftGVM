package br.com.mura.betaminecraftmod.client;

import net.minecraft.client.Minecraft;
import org.joml.Vector3f;

public final class EraVisuals {
    // Published to chunk compilation workers; never read Minecraft/player state there.
    private static volatile EraVisualProfile profile = EraVisualProfile.MODERN;
    private static float previousFogBrightness = 1, fogBrightness = 1;
    public static EraVisualProfile active() { return profile; }
    public static boolean legacyLeaves(){int era=br.com.mura.betaminecraftmod.progression.CampaignRules.era(Minecraft.getInstance().level);return era>=0&&era<=6;}
    public static void select(int era) {
        var next = EraVisualProfile.forEra(era);
        if (next != profile) previousFogBrightness = fogBrightness = 1;
        profile = next;
    }
    public static void tick() {
        var minecraft = Minecraft.getInstance();
        if (!profile.historical() || minecraft.level == null || minecraft.player == null) return;
        previousFogBrightness = fogBrightness;
        float distance = profile.viewDistance(minecraft.options.getEffectiveRenderDistance());
        float option = (float)(Math.log(256 / distance) / Math.log(2));
        float blend = (3 - Math.clamp(option, 0, 3)) / 3;
        int light = minecraft.level.getMaxLocalRawBrightness(minecraft.player.blockPosition());
        float target = profile.brightness(light) * (1 - blend) + blend;
        fogBrightness += (target - fogBrightness) * .1f;
    }
    public static float fogBrightness(float partial) { return previousFogBrightness + (fogBrightness - previousFogBrightness) * partial; }
    public static Vector3f sky(long time, float partial) {
        float day = profile.daylight(time, partial);
        return rgb(profile.skyColor()).mul(day);
    }
    public static Vector3f rgb(int color) {
        return new Vector3f((color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
    }
    private EraVisuals() { }
}
