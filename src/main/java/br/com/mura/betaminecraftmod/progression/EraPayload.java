package br.com.mura.betaminecraftmod.progression;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "betaminecraftmod")
public record EraPayload(int era,String firstRelease,long seed,int dimension) implements CustomPacketPayload {
    public EraPayload(int era,String firstRelease,long seed,boolean nether){this(era,firstRelease,seed,nether?-1:0);}
    public EraPayload(int era,String firstRelease){this(era,firstRelease,0,false);}
    public EraPayload(int era){this(era,"1.0");}
    public static EraPayload forLevel(int era,net.minecraft.server.level.ServerLevel level){return new EraPayload(era,CampaignRules.firstRelease(level),level.getServer().overworld().getSeed(),level.dimension()==net.minecraft.world.level.Level.NETHER?-1:level.dimension()==net.minecraft.world.level.Level.END?1:0);}
    public static final Type<EraPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("betaminecraftmod", "era"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EraPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, EraPayload::era,ByteBufCodecs.stringUtf8(16),EraPayload::firstRelease,ByteBufCodecs.LONG,EraPayload::seed,ByteBufCodecs.VAR_INT,EraPayload::dimension,EraPayload::new);
    @Override public Type<EraPayload> type() { return TYPE; }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("5").playToClient(TYPE, CODEC, (payload, context) -> context.enqueueWork(() -> {
            br.com.mura.betaminecraftmod.client.HistoricalVegetationColors.configure(payload.seed(),payload.dimension());
            CampaignRules.clientFirstRelease(payload.firstRelease());
            context.player().getPersistentData().putInt(CampaignRules.ERA_TAG, payload.era());
            br.com.mura.betaminecraftmod.client.EraWorldEntry.confirmed();
            br.com.mura.betaminecraftmod.client.EraTextures.select(payload.era());
        }));
    }
}
