package br.com.mura.betaminecraftmod.quests;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** State is produced exclusively by the server. Clients cannot submit completion masks. */
public record TreeQuestStatePayload(int era,int mask,boolean won,int phase,int wave,int living,int remaining,boolean advanceReady) implements CustomPacketPayload {
    public static final Type<TreeQuestStatePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("betaminecraftmod","quest_tree"));
    public static final StreamCodec<RegistryFriendlyByteBuf,TreeQuestStatePayload> CODEC=new StreamCodec<>(){
        public TreeQuestStatePayload decode(RegistryFriendlyByteBuf b){return new TreeQuestStatePayload(b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean());}
        public void encode(RegistryFriendlyByteBuf b,TreeQuestStatePayload p){b.writeVarInt(p.era);b.writeVarInt(p.mask);b.writeBoolean(p.won);b.writeVarInt(p.phase);b.writeVarInt(p.wave);b.writeVarInt(p.living);b.writeVarInt(p.remaining);b.writeBoolean(p.advanceReady);}
    };
    @Override public Type<TreeQuestStatePayload> type(){return TYPE;}
}
