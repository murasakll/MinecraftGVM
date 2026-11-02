package br.com.mura.betaminecraftmod.quests;

import br.com.mura.betaminecraftmod.progression.InfdevContent;
import br.com.mura.betaminecraftmod.progression.CampaignRules;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "betaminecraftmod")
public final class QuestService {
    private record Snapshot(int era, QuestProgress progress) { }
    private static QuestChapter chapter(ServerPlayer player) { return QuestChapter.get(CampaignRules.era(player)); }
    private static boolean active(ServerPlayer player) { return chapter(player) != null; }
    private static final Map<ServerPlayer, Snapshot> LAST_SENT = new WeakHashMap<>();
    public static QuestProgress read(ServerPlayer p) {
        return read(p, chapter(p));
    }
    private static QuestProgress read(ServerPlayer p, QuestChapter chapter) {
        if (chapter == null) return QuestProgress.EMPTY;
        String KEY = chapter.storagePrefix();
        var t = p.getPersistentData();
        return new QuestProgress(t.getIntOr(KEY + "stage", 0), t.getIntOr(KEY + "wood", 0), t.getBooleanOr(KEY + "tool", false),
            t.getIntOr(KEY + "placed", 0), t.getBooleanOr(KEY + "shelter", false), t.getBooleanOr(KEY + "opened", false));
    }
    private static void write(ServerPlayer p, QuestProgress s) {
        var chapter = chapter(p);
        if (chapter == null) return;
        String KEY = chapter.storagePrefix();
        var t = p.getPersistentData();
        t.putInt(KEY + "stage", s.stage()); t.putInt(KEY + "wood", s.wood()); t.putBoolean(KEY + "tool", s.tool());
        t.putInt(KEY + "placed", s.placed()); t.putBoolean(KEY + "shelter", s.sheltered()); t.putBoolean(KEY + "opened", s.opened());
    }
    private static void sync(ServerPlayer player, QuestProgress state) {
        mirrorAdvancements(player, state);
        var snapshot = new Snapshot(CampaignRules.era(player), state);
        if (!snapshot.equals(LAST_SENT.put(player, snapshot))) PacketDistributor.sendToPlayer(player, new QuestStatePayload(snapshot.era(), state));
    }
    private static void mirrorAdvancements(ServerPlayer player, QuestProgress state) {
        var manager = player.level().getServer().getAdvancements();
        for (int index = -1; index < state.stage(); index++) {
            var id = net.minecraft.resources.Identifier.fromNamespaceAndPath("betaminecraftmod", chapter(player).path() + "/" + (index < 0 ? "root" : "quest_" + index));
            var holder = manager.get(id);
            if (holder != null && !player.getAdvancements().getOrStartProgress(holder).isDone())
                player.getAdvancements().award(holder, "completed");
        }
    }
    /** Operator shortcut: skip objectives without granting their item rewards. */
    public static boolean skipAndAdvance(ServerPlayer player) {
        if (!CampaignRules.isCampaign(player.level()) || !active(player)) return false;
        var progress = br.com.mura.betaminecraftmod.progression.CampaignProgress.get(player.level());
        var next = br.com.mura.betaminecraftmod.progression.EraCatalog.get(progress.rings() + 1);
        if (next == null || !next.playable()) return false;
        // The server command runs serially with progression; validate before touching quest state.
        write(player, read(player).withStage(4));
        LAST_SENT.remove(player);
        sync(player, read(player));
        if (!progress.unlockRing(player.level())) throw new IllegalStateException("Validated era could not advance");
        write(player, read(player).open());
        for (var other : player.level().getServer().getPlayerList().getPlayers()) {
            if (active(other)) { LAST_SENT.remove(other); sync(other, read(other)); }
        }
        return true;
    }
    public static void action(ServerPlayer player, QuestActionPayload request) {
        if (QuestTreeService.active(player)) { QuestTreeService.action(player, request); return; }
        if (!active(player)) return;
        var chapter = chapter(player);
        if (request.era() != chapter.era()) { LAST_SENT.remove(player); sync(player, read(player)); return; }
        QuestProgress before = read(player), after;
        if (request.claim() && request.expectedStage() == 4 && before.stage() == 4) {
            if (!br.com.mura.betaminecraftmod.progression.CampaignProgress.get(player.level()).unlockRing(player.level()))
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("A pr\u00f3xima era ainda aguarda seu gerador hist\u00f3rico. Voc\u00ea pode continuar na Infdev."));
            else {
                write(player, read(player).open());
                for (var other : player.level().getServer().getPlayerList().getPlayers()) {
                    if (active(other)) { LAST_SENT.remove(other); sync(other, read(other)); }
                }
            }
            return;
        }
        if (!request.claim()) after = before.open();
        else {
            after = before.claim(request.era(), request.expectedStage(), chapter);
            if (after.stage() != before.stage()) {
                write(player, after); // Commit before rewarding; replayed stage packets are rejected.
                ItemStack reward = switch (before.stage()) {
                    case 1 -> new ItemStack(chapter.era() == 2 ? Items.COAL : Items.STICK, chapter.era() == 2 ? 8 : 4); case 2 -> new ItemStack(Items.TORCH, chapter.era() == 2 ? 16 : 4); case 3 -> new ItemStack(Items.BREAD, chapter.era() == 2 ? 8 : 4); default -> ItemStack.EMPTY;
                };
                if (!reward.isEmpty() && !player.getInventory().add(reward)) player.drop(reward, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
        write(player, after);
        LAST_SENT.remove(player); sync(player, after);
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !active(player) || player.tickCount % 20 != 0) return;
        QuestProgress state = read(player);
        var chapter = chapter(player);
        int logs = 0;
        for (var stack : player.getInventory().getNonEquipmentItems()) if (stack.is(chapter.era() == 0 ? Items.OAK_LOG : chapter.era() == 1 ? Items.COBBLESTONE : Items.IRON_INGOT)) logs += stack.getCount();
        state = state.wood(Math.min(chapter.materials(), logs));
        if (!state.sheltered() && state.placed() >= chapter.blocks() && (chapter.era() == 2 ? poweredCircuit(player) : shelter(player))) state = state.shelter();
        write(player, state); sync(player, state);
    }
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        ItemStack stack = event.getCrafting();
        if (chapter(player).era() == 2 ? stack.is(Items.IRON_PICKAXE) : chapter(player).era() == 1 ? stack.is(Items.STONE_PICKAXE)
                : stack.is(Items.WOODEN_PICKAXE) || stack.is(Items.WOODEN_AXE) || stack.is(Items.WOODEN_SHOVEL) || stack.is(Items.WOODEN_HOE)) {
            var state = read(player).crafted(); write(player, state); sync(player, state);
        }
    }
    @SubscribeEvent public static void placed(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || !active(player) || !(chapter(player).era() == 2 ? event.getPlacedBlock().is(net.minecraft.world.level.block.Blocks.REDSTONE_WIRE) : event.getPlacedBlock().isSolidRender())
                || !InfdevContent.allowed(new ItemStack(event.getPlacedBlock().getBlock()), chapter(player).era())) return;
        if (read(player).placed() < chapter(player).blocks()) write(player, read(player).place());
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer from && event.getEntity() instanceof ServerPlayer to) {
            for (int era = 0; era <= 2; era++) {
                String prefix = QuestChapter.get(era).storagePrefix();
                var original = from.getPersistentData();
                for (String suffix : new String[]{"stage", "wood", "tool", "placed", "shelter", "opened"}) {
                    var value = original.get(prefix + suffix);
                    if (value != null) to.getPersistentData().put(prefix + suffix, value.copy());
                }
            }
            LAST_SENT.remove(to);
        }
    }
    @SubscribeEvent public static void joined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)) { LAST_SENT.remove(player); sync(player, read(player)); }
    }
    static boolean poweredCircuit(ServerPlayer player) {
        var origin = player.blockPosition();
        for (var pos : net.minecraft.core.BlockPos.betweenClosed(origin.offset(-5,-5,-5), origin.offset(5,5,5))) {
            if (!player.level().hasChunkAt(pos)) continue;
            var state=player.level().getBlockState(pos);
            if (state.is(net.minecraft.world.level.block.Blocks.REDSTONE_WIRE)
                    && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) > 0) return true;
        }
        return false;
    }
    static boolean shelter(ServerPlayer player) {
        var pos = player.blockPosition(); var level = player.level();
        boolean roof = false;
        for (int y = 2; y <= 6; y++) if (level.hasChunkAt(pos.above(y)) && level.getBlockState(pos.above(y)).isSolidRender()) { roof = true; break; }
        if (!roof) return false;
        int walls = 0;
        for (var dir : new net.minecraft.core.Direction[]{net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.WEST})
            for (int d = 1; d <= 4; d++) if (level.hasChunkAt(pos.relative(dir,d)) && (level.getBlockState(pos.relative(dir, d)).isSolidRender() || level.getBlockState(pos.relative(dir, d).above()).isSolidRender())) { walls++; break; }
        return walls >= 3;
    }
}
