package br.com.mura.betaminecraftmod.client.visual;

import br.com.mura.betaminecraftmod.client.HistoricalLightmapState;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LightmapRenderState.class)
public abstract class HistoricalLightmapStateMixin implements HistoricalLightmapState {
    @Unique private int[] campaign$pixels;
    @Override public int[] betaminecraftmod$pixels(){return campaign$pixels;}
    @Override public void betaminecraftmod$pixels(int[] pixels){campaign$pixels=pixels;}
}
