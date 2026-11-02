package br.com.mura.betaminecraftmod.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ContainerLabelAccessor {
    @Accessor("titleLabelX") void campaign$setTitleX(int x);
    @Accessor("titleLabelY") void campaign$setTitleY(int y);
}
