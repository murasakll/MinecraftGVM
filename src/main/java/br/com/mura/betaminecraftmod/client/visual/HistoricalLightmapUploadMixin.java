package br.com.mura.betaminecraftmod.client.visual;

import br.com.mura.betaminecraftmod.client.HistoricalLightmapState;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public abstract class HistoricalLightmapUploadMixin {
    @Shadow @Final private GpuTexture texture;
    @Unique private NativeImage campaign$image;
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void campaign$upload(LightmapRenderState state,CallbackInfo ci) {
        int[] pixels=((HistoricalLightmapState)state).betaminecraftmod$pixels();if(pixels==null)return;
        if(state.needsUpdate){
            if(campaign$image==null)campaign$image=new NativeImage(16,16,false);
            for(int y=0;y<16;y++)for(int x=0;x<16;x++)campaign$image.setPixel(x,y,pixels[x+y*16]);
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture,campaign$image);
        }
        ci.cancel();
    }
    @Inject(method="close",at=@At("HEAD"))
    private void campaign$close(CallbackInfo ci){if(campaign$image!=null){campaign$image.close();campaign$image=null;}}
}
