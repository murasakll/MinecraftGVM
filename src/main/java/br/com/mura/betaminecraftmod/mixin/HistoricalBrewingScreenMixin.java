package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandScreen.class)
public abstract class HistoricalBrewingScreenMixin extends AbstractContainerScreen<BrewingStandMenu> {
    @Unique private static final Identifier campaign$sheet=Identifier.fromNamespaceAndPath("betaminecraftmod","textures/gui/brewing_1_0.png");
    protected HistoricalBrewingScreenMixin(BrewingStandMenu menu,Inventory inventory,Component title){super(menu,inventory,title);}
    @Inject(method="init",at=@At("TAIL"))
    private void campaign$layout(CallbackInfo ci){
        if(!ReleaseBrewing.clientActive())return;titleLabelX=56;titleLabelY=6;
        for(int i=0;i<3;i++){var slot=(SlotPositionAccessor)menu.getSlot(i);slot.campaign$setX(i==0?56:i==1?79:102);slot.campaign$setY(i==1?53:46);}
    }
    @Inject(method="extractBackground",at=@At("HEAD"),cancellable=true)
    private void campaign$background(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial,CallbackInfo ci){
        if(!ReleaseBrewing.clientActive())return;ci.cancel();super.extractBackground(graphics,mouseX,mouseY,partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED,campaign$sheet,leftPos,topPos,0,0,imageWidth,imageHeight,256,256);
        int ticks=menu.getBrewingTicks();if(ticks<=0)return;
        int arrow=(int)(28*(1-ticks/(float)ReleaseBrewingCatalog.BREW_TICKS));if(arrow>0)graphics.blit(RenderPipelines.GUI_TEXTURED,campaign$sheet,leftPos+97,topPos+16,176,0,9,arrow,256,256);
        int bubble=switch(ticks/2%7){case 0->29;case 1->24;case 2->20;case 3->16;case 4->11;case 5->6;default->0;};
        if(bubble>0)graphics.blit(RenderPipelines.GUI_TEXTURED,campaign$sheet,leftPos+65,topPos+14+29-bubble,185,29-bubble,12,bubble,256,256);
    }
}
