package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.ReleaseBrewing;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreen.class)
public abstract class HistoricalEnchantingScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {
    @Unique private static final Identifier campaign$sheet=Identifier.fromNamespaceAndPath("betaminecraftmod","textures/gui/enchanting_1_0.png");
    @Unique private static final Identifier campaign$book=Identifier.fromNamespaceAndPath("betaminecraftmod","textures/gui/enchanting_book_1_0.png");
    @Unique private static final FontDescription campaign$glyphs=new FontDescription.Resource(Identifier.fromNamespaceAndPath("betaminecraftmod","enchanting_1_0"));
    @Shadow private void extractBook(GuiGraphicsExtractor graphics,int left,int top){throw new AssertionError();}
    protected HistoricalEnchantingScreenMixin(EnchantmentMenu menu,Inventory inventory,Component title){super(menu,inventory,title);}
    @ModifyArg(method="extractBook",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;book(Lnet/minecraft/client/model/object/book/BookModel;Lnet/minecraft/resources/Identifier;FFFIIII)V"),index=1)
    private Identifier campaign$bookTexture(Identifier nativeTexture){return ReleaseBrewing.clientActive()?campaign$book:nativeTexture;}
    @Override protected void extractLabels(GuiGraphicsExtractor graphics,int mouseX,int mouseY) {
        if(!ReleaseBrewing.clientActive()){super.extractLabels(graphics,mouseX,mouseY);return;}
        graphics.text(font,"Enchant",12,6,0xFF404040,false);graphics.text(font,"Inventory",8,imageHeight-94,0xFF404040,false);
    }
    @Inject(method="init",at=@At("TAIL"))
    private void campaign$layout(CallbackInfo ci){if(ReleaseBrewing.clientActive()){((SlotPositionAccessor)menu.getSlot(0)).campaign$setX(25);titleLabelX=12;}}
    @Inject(method="extractBackground",at=@At("HEAD"),cancellable=true)
    private void campaign$background(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial,CallbackInfo ci) {
        if(!ReleaseBrewing.clientActive())return;ci.cancel();super.extractBackground(graphics,mouseX,mouseY,partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED,campaign$sheet,leftPos,topPos,0,0,imageWidth,imageHeight,256,256);extractBook(graphics,leftPos,topPos);
        var random=new Random(((br.com.mura.betaminecraftmod.progression.HistoricalEnchantingMenuAccess)menu).campaign$labelSeed());
        for(int row=0;row<3;row++) {
            String words=br.com.mura.betaminecraftmod.progression.ReleaseEnchantingNames.next(random);
            int x=leftPos+60,y=topPos+14+19*row,cost=menu.costs[row];boolean enabled=cost>0&&minecraft.player.experienceLevel>=cost;
            boolean hovered=mouseX>=x&&mouseX<x+108&&mouseY>=y&&mouseY<y+19;
            graphics.blit(RenderPipelines.GUI_TEXTURED,campaign$sheet,x,y,0,!enabled?185:hovered?204:166,108,19,256,256);
            if(cost<=0)continue;
            int color=!enabled?(6839882&16711422)>>1:hovered?16777088:6839882;
            var text=Component.literal(words).withStyle(Style.EMPTY.withFont(campaign$glyphs));
            graphics.textWithWordWrap(font,text,x+2,y+2,104,0xFF000000|color,false);
            String value=Integer.toString(cost);graphics.text(font,value,x+106-font.width(value),y+9,0xFF000000|(!enabled?4226832:8453920));
        }
    }
    @Inject(method="extractRenderState",at=@At("HEAD"),cancellable=true)
    private void campaign$noModernHints(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial,CallbackInfo ci) {
        if(!ReleaseBrewing.clientActive())return;ci.cancel();super.extractRenderState(graphics,mouseX,mouseY,partial);
    }
}
