package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.client.EraTextures;
import br.com.mura.betaminecraftmod.InfdevLoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep vanilla reload ticking, completion and error recovery; replace only our reload presentation. */
@Mixin(LoadingOverlay.class)
public abstract class EraLoadingOverlayMixin {
    @Shadow private long fadeInStart;
    @Shadow protected long fadeOutStart;
    @Shadow @Final private ReloadInstance reload;
    @ModifyArg(method="tick",at=@At(value="INVOKE",target="Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"),index=0)
    private Object campaign$reportFailure(Object result) {
        if (result instanceof java.util.Optional<?> optional && optional.orElse(null) instanceof Throwable error)
            EraTextures.reloadFailed(this,error);
        return result;
    }
    @Inject(method="extractRenderState",at=@At("HEAD"),cancellable=true)
    private void campaign$loading(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta,CallbackInfo ci) {
        if (!EraTextures.ownsOverlay(this)) return;
        if (fadeInStart == -1) fadeInStart=Util.getMillis()-1001;
        InfdevLoadingScreen.draw(graphics,graphics.guiWidth(),graphics.guiHeight(),reload.getActualProgress(),"Preparing resources",false);
        // tick() has already executed the native completion callback before this flag is set.
        if (fadeOutStart != -1 && Minecraft.getInstance().gui.overlay() == (Object)this)
            Minecraft.getInstance().gui.setOverlay(null);
        ci.cancel();
    }
}
