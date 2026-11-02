package br.com.mura.betaminecraftmod.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** Registry-only codec context; adds no fields to existing world generator saves. */
public record ModernClimateContext(HolderGetter<NormalNoise> noises,
        Holder<NoiseGeneratorSettings> settings, Holder<MultiNoiseBiomeSourceParameterList> parameters) {
    public static final MapCodec<ModernClimateContext> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        RegistryOps.retrieveGetter(Registries.NOISE),
        RegistryOps.retrieveElement(NoiseGeneratorSettings.OVERWORLD),
        RegistryOps.retrieveElement(MultiNoiseBiomeSourceParameterLists.OVERWORLD)
    ).apply(i,ModernClimateContext::new));
    public static ModernClimateContext from(HolderLookup.Provider registry){
        return new ModernClimateContext(registry.lookupOrThrow(Registries.NOISE),
            registry.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD),
            registry.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD));
    }
    public MultiNoiseBiomeSource source(){return MultiNoiseBiomeSource.createFromPreset(parameters);}
    public BiomeResolver resolver(long seed){return source().createUncachedResolver(RandomState.create(noises,seed,settings.value()));}
}
