package br.com.mura.betaminecraftmod.quests;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record QuestActionPayload(int era, int expectedStage, boolean claim) implements CustomPacketPayload {
    public static final Type<QuestActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("betaminecraftmod", "quest_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestActionPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, QuestActionPayload::era, ByteBufCodecs.VAR_INT, QuestActionPayload::expectedStage, ByteBufCodecs.BOOL, QuestActionPayload::claim, QuestActionPayload::new);
    @Override public Type<QuestActionPayload> type() { return TYPE; }
}
