package br.com.mura.betaminecraftmod.client;

import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Optional Iris API access: the distributed mod does not depend on Iris or Sodium. */
@EventBusSubscriber(modid = "betaminecraftmod", value = Dist.CLIENT)
public final class ShaderCompatibility {
    private static volatile boolean active, unavailable;
    private static boolean checked, notified;
    private static Object api;
    private static Method inUse;
    private static int ticks;
    public static boolean overridesLighting() { return active || unavailable; }
    public static boolean shaderPackActive() { return active; }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (++ticks % 20 != 0) return;
        if (!checked) {
            checked = true;
            if (ModList.get().isLoaded("iris")) {
                try {
                    Class<?> type = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                    api = type.getMethod("getInstance").invoke(null);
                    inUse = type.getMethod("isShaderPackInUse");
                } catch (ReflectiveOperationException | LinkageError exception) {
                    unavailable = true;
                    com.mojang.logging.LogUtils.getLogger().warn("Cannot query Iris shader status; preserving shader-controlled lighting", exception);
                }
            }
        }
        if (inUse != null) {
            try { active = (boolean)inUse.invoke(api); }
            catch (ReflectiveOperationException exception) { unavailable = true; inUse = null; }
        }
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !ClientEraState.active()) { notified = false; return; }
        if (overridesLighting() && !notified) {
            minecraft.player.sendSystemMessage(Component.literal(unavailable ? "Não foi possível verificar o shader. Mantendo a iluminação compatível." : "Shader ativo: a luz, o céu e a neblina seguem o shader. Texturas e regras da era continuam ativas."));
            notified = true;
        } else if (!overridesLighting()) notified = false;
    }
}
