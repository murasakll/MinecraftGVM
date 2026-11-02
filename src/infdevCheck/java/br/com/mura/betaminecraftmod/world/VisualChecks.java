package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.client.EraVisualProfile;

public final class VisualChecks {
    public static void run() {
        var profile = EraVisualProfile.INFDEV;
        float previous = 0;
        for (int level = 0; level < 16; level++) {
            float expected = level / (60f - 3 * level) * .95f + .05f;
            if (Math.abs(profile.brightness(level) - expected) > .000001f || profile.brightness(level) < previous)
                throw new AssertionError("Infdev brightness at " + level);
            previous = profile.brightness(level);
        }
        if (profile.skyDarkening(6000, 0) != 0 || profile.skyDarkening(18000, 0) != 11)
            throw new AssertionError("Infdev day/night sky subtraction");
        if (profile.viewDistance(32) != 256 || profile.viewDistance(4) != 64 || profile.ambientOcclusion())
            throw new AssertionError("Infdev fog/AO profile");
        if (EraVisualProfile.forEra(-1).historical() || EraVisualProfile.forEra(3).historical())
            throw new AssertionError("Historical visuals leaked to another era");
        System.out.println("Visual profiles: 16 light levels, day/night, distance and era isolation passed.");
    }
}
