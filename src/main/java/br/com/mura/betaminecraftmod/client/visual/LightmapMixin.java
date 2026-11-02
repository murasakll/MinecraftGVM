package br.com.mura.betaminecraftmod.client.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import br.com.mura.betaminecraftmod.client.EraVisuals;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapMixin {
    @org.spongepowered.asm.mixin.Unique private float campaign$flicker;
    @org.spongepowered.asm.mixin.Unique private final java.util.Random campaign$random=new java.util.Random();
    @Inject(method = "tick", at = @At("TAIL"))
    private void campaign$tick(CallbackInfo ci) {
        EraVisuals.tick();
        campaign$flicker=(float)(campaign$flicker+(campaign$random.nextDouble()-campaign$random.nextDouble())*campaign$random.nextDouble()*campaign$random.nextDouble());
        campaign$flicker=(float)(campaign$flicker*.9);
    }
    @Inject(method = "extract", at = @At("TAIL"))
    private void campaign$light(LightmapRenderState state, float partial, CallbackInfo ci) {
        var historical=(br.com.mura.betaminecraftmod.client.HistoricalLightmapState)state;historical.betaminecraftmod$pixels(null);
        var mc=Minecraft.getInstance();var level=mc.level;
        int era=br.com.mura.betaminecraftmod.progression.CampaignRules.era(level);
        if(level==null||br.com.mura.betaminecraftmod.client.ShaderCompatibility.overridesLighting()||!br.com.mura.betaminecraftmod.client.HistoricalLighting.active(era,br.com.mura.betaminecraftmod.progression.CampaignRules.firstRelease(level)))return;
        int dimension=level.dimension()==net.minecraft.world.level.Level.NETHER?-1:level.dimension()==net.minecraft.world.level.Level.END?1:0;
        // Original lightmap and world sky subtraction sample the completed tick (1.0), not camera interpolation.
        float lightPartial=1;
        float rain=era>=4?level.getRainLevel(lightPartial):0,thunder=era>=4?level.getThunderLevel(lightPartial):0;
        long time=level.getOverworldClockTime();
        float daylight=br.com.mura.betaminecraftmod.client.HistoricalLighting.sky(time,lightPartial,dimension,rain,thunder,era>=5);
        int darkening=(int)((1-br.com.mura.betaminecraftmod.client.HistoricalLighting.sky(time,lightPartial,dimension,rain,thunder,false))*11);
        boolean flash=((HistoricalFlashAccessor)level).campaign$skyFlashTime()>0&&!mc.options.hideLightningFlash().get();
        var inputs=new br.com.mura.betaminecraftmod.client.HistoricalLighting.Inputs(era,dimension,daylight,darkening,campaign$flicker,mc.options.gamma().get().floatValue(),flash);
        historical.betaminecraftmod$pixels(br.com.mura.betaminecraftmod.client.HistoricalLighting.pixels(inputs));
        state.needsUpdate = true;
    }
}
