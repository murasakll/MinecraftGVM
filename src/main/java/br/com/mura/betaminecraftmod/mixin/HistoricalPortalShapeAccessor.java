package br.com.mura.betaminecraftmod.mixin;

import net.minecraft.core.*;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PortalShape.class)
public interface HistoricalPortalShapeAccessor {
    @Invoker("<init>")
    static PortalShape campaign$create(Direction.Axis axis,int count,Direction right,BlockPos bottom,int width,int height){throw new AssertionError();}
}
