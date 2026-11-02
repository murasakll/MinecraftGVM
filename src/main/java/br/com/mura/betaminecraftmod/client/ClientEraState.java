package br.com.mura.betaminecraftmod.client;

import br.com.mura.betaminecraftmod.progression.GenerationRouter;
import java.util.Locale;
import net.minecraft.network.chat.Component;

/** Updated only by the server's era payload; inactive until a campaign is synchronized. */
public final class ClientEraState {
    private static int era = -1;
    private ClientEraState() { }
    public static void setEra(int value) {
        era = value >= 0 && value < GenerationRouter.Era.values().length ? value : -1;
    }
    public static boolean active() { return era >= 0; }
    public static Component name() {
        if (!active()) return Component.empty();
        return Component.translatable(br.com.mura.betaminecraftmod.progression.EraCatalog.get(era).translationKey());
    }
}
