package br.com.mura.betaminecraftmod.quests;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "betaminecraftmod")
public record QuestStatePayload(int era, QuestProgress progress) implements CustomPacketPayload {
    public static final Type<QuestStatePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("betaminecraftmod", "quest_state"));
    private static final StreamCodec<RegistryFriendlyByteBuf, QuestProgress> STATE = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, QuestProgress::stage, ByteBufCodecs.VAR_INT, QuestProgress::wood,
        ByteBufCodecs.BOOL, QuestProgress::tool, ByteBufCodecs.VAR_INT, QuestProgress::placed,
        ByteBufCodecs.BOOL, QuestProgress::sheltered, ByteBufCodecs.BOOL, QuestProgress::opened, QuestProgress::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestStatePayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, QuestStatePayload::era, STATE, QuestStatePayload::progress, QuestStatePayload::new);
    @Override public Type<QuestStatePayload> type() { return TYPE; }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("5");
        registrar.playToClient(TreeQuestStatePayload.TYPE, TreeQuestStatePayload.CODEC, (payload, context) -> context.enqueueWork(() -> {
            br.com.mura.betaminecraftmod.client.QuestScreen.treeState = payload;
        }));
        registrar.playToClient(TYPE, CODEC, (payload, context) -> context.enqueueWork(() -> {
            br.com.mura.betaminecraftmod.client.QuestScreen.chapterEra = payload.era();
            br.com.mura.betaminecraftmod.client.QuestScreen.progress = payload.progress();
            br.com.mura.betaminecraftmod.client.QuestScreen.synchronizedState = true;
        }));
        registrar.playToServer(QuestActionPayload.TYPE, QuestActionPayload.CODEC, (payload, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) QuestService.action(player, payload);
        }));
    }
}
