package br.com.mura.betaminecraftmod.client.visual;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
public interface HistoricalFlashAccessor {
    @Accessor("skyFlashTime") int campaign$skyFlashTime();
}
