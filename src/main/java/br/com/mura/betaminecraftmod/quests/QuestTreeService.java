package br.com.mura.betaminecraftmod.quests;

import br.com.mura.betaminecraftmod.progression.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

@EventBusSubscriber(modid="betaminecraftmod")
public final class QuestTreeService {
    public static final String KEY="betaminecraftmod:quest_trees";
    private record Position(int era,double x,double z){}
    private static final Map<ServerPlayer,Position> POSITIONS=new WeakHashMap<>();
    private static final Map<ServerPlayer,TreeQuestStatePayload> SENT=new WeakHashMap<>();
    public static boolean active(ServerPlayer p){return !QuestTree.goals(CampaignRules.era(p)).isEmpty();}
    private static CompoundTag data(ServerPlayer p){return p.getPersistentData().getCompoundOrEmpty(KEY);}
    private static String field(ServerPlayer p,String suffix){return CampaignRules.era(p)+"_"+suffix;}
    public static int fact(ServerPlayer p,QuestTree.Fact f){return data(p).getIntOr(field(p,f.name()),0);}
    public static void observe(ServerPlayer p,QuestTree.Fact f,int value){if(!active(p))return;var t=data(p);t.putInt(field(p,f.name()),Math.max(fact(p,f),Math.clamp(value,0,1000000)));p.getPersistentData().put(KEY,t);}
    public static Set<String> completed(ServerPlayer p){return QuestTree.fromMask(CampaignRules.era(p),data(p).getIntOr(field(p,"mask"),0));}
    public static boolean ready(ServerPlayer p){return QuestTree.ready(CampaignRules.era(p),completed(p));}
    private static void migrate(ServerPlayer p){var t=data(p);String migrated=field(p,"migrated");if(t.getBooleanOr(migrated,false))return;var legacy=QuestService.read(p);int era=CampaignRules.era(p);t.putBoolean(migrated,true);p.getPersistentData().put(KEY,t);if(legacy.opened())observe(p,QuestTree.Fact.OPEN,1);observe(p,QuestTree.Fact.MATERIAL,legacy.wood());
        // In the old Alpha chapter, placed meant redstone wires and sheltered meant powered circuit.
        if(era<2){observe(p,QuestTree.Fact.PLACED,legacy.placed());if(legacy.sheltered())observe(p,QuestTree.Fact.SHELTER,1);}else if(legacy.sheltered())observe(p,QuestTree.Fact.CIRCUIT,1);
        if(era>=1&&legacy.tool())observe(p,QuestTree.Fact.TOOL,1);
    }
    private static int count(ServerPlayer p,Item...items){int n=0;for(var stack:p.getInventory().getNonEquipmentItems()){if(!HistoricalContent.allowed(stack,CampaignRules.era(p)))continue;for(var item:items)if(stack.is(item)){n+=stack.getCount();break;}}return n;}
    public static void refresh(ServerPlayer p){if(!active(p))return;migrate(p);int era=CampaignRules.era(p);
        observe(p,QuestTree.Fact.MATERIAL,count(p,era==0?Items.OAK_LOG:era==1?Items.COBBLESTONE:Items.IRON_INGOT));
        observe(p,QuestTree.Fact.TOOL,era>=2?count(p,Items.IRON_PICKAXE,Items.DIAMOND_PICKAXE):count(p,Items.STONE_PICKAXE,Items.IRON_PICKAXE,Items.DIAMOND_PICKAXE));
        observe(p,QuestTree.Fact.STONE,count(p,Items.COBBLESTONE));observe(p,QuestTree.Fact.TORCH,count(p,Items.TORCH));
        observe(p,QuestTree.Fact.SWORD,count(p,Items.STONE_SWORD,Items.IRON_SWORD,Items.DIAMOND_SWORD));observe(p,QuestTree.Fact.BOW,count(p,Items.BOW));
        observe(p,QuestTree.Fact.FOOD,count(p,Items.BREAD,Items.PORKCHOP,Items.COOKED_PORKCHOP,Items.APPLE,Items.MUSHROOM_STEW,Items.COD,Items.COOKED_COD,Items.BEEF,Items.COOKED_BEEF,Items.CHICKEN,Items.COOKED_CHICKEN));
        observe(p,QuestTree.Fact.COAL,count(p,Items.COAL));observe(p,QuestTree.Fact.FURNACE,count(p,Items.FURNACE));
        observe(p,QuestTree.Fact.REDSTONE,count(p,Items.REDSTONE));observe(p,QuestTree.Fact.DIAMOND,count(p,Items.DIAMOND));
        observe(p,QuestTree.Fact.ARROW,count(p,Items.ARROW));observe(p,QuestTree.Fact.CHEST,count(p,Items.CHEST));
        if(QuestService.shelter(p)){observe(p,QuestTree.Fact.SHELTER,1);if(p.level().dimension().equals(Level.NETHER))observe(p,QuestTree.Fact.NETHER_SHELTER,1);}
        if(era>=3)advancedFacts(p);
        if(era>=2)nearbyFacts(p);
        var complete=QuestTree.completed(era,f->fact(p,f),completed(p));var t=data(p);t.putInt(field(p,"mask"),QuestTree.mask(era,complete));p.getPersistentData().put(KEY,t);
        mirror(p,complete);sync(p,false);
    }
    private static void advancedFacts(ServerPlayer p){
        Object[][] items={{QuestTree.Fact.NETHERRACK,Items.NETHERRACK},{QuestTree.Fact.SOUL_SAND,Items.SOUL_SAND},{QuestTree.Fact.GLOWSTONE,Items.GLOWSTONE},{QuestTree.Fact.LAPIS,Items.LAPIS_LAZULI},{QuestTree.Fact.PISTON,Items.PISTON},{QuestTree.Fact.BREAD,Items.BREAD},{QuestTree.Fact.WHEAT,Items.WHEAT},{QuestTree.Fact.FENCE_GATE,Items.OAK_FENCE_GATE},{QuestTree.Fact.ENDER_PEARL,Items.ENDER_PEARL},{QuestTree.Fact.DIAMOND_SWORD,Items.DIAMOND_SWORD},{QuestTree.Fact.DIAMOND_PICKAXE,Items.DIAMOND_PICKAXE},{QuestTree.Fact.BLAZE_ROD,Items.BLAZE_ROD},{QuestTree.Fact.NETHER_WART,Items.NETHER_WART},{QuestTree.Fact.BREWING_STAND,Items.BREWING_STAND},{QuestTree.Fact.EYES,Items.ENDER_EYE},{QuestTree.Fact.ENCHANTING_TABLE,Items.ENCHANTING_TABLE},{QuestTree.Fact.BOOKSHELF,Items.BOOKSHELF}};
        for(var pair:items)observe(p,(QuestTree.Fact)pair[0],count(p,(Item)pair[1]));
        int armor=0;for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){var stack=p.getItemBySlot(slot);if(stack.is(Items.IRON_HELMET)||stack.is(Items.IRON_CHESTPLATE)||stack.is(Items.IRON_LEGGINGS)||stack.is(Items.IRON_BOOTS)||stack.is(Items.DIAMOND_HELMET)||stack.is(Items.DIAMOND_CHESTPLATE)||stack.is(Items.DIAMOND_LEGGINGS)||stack.is(Items.DIAMOND_BOOTS))armor++;}observe(p,QuestTree.Fact.IRON_ARMOR,armor);
        for(var stack:p.getInventory().getNonEquipmentItems()){if(!HistoricalContent.allowed(stack,CampaignRules.era(p)))continue;var potion=stack.get(DataComponents.POTION_CONTENTS);if(stack.is(Items.POTION)&&potion!=null&&potion.hasEffects())observe(p,QuestTree.Fact.POTION,1);if(stack.isEnchanted())observe(p,QuestTree.Fact.ENCHANTED,1);}
        for(var slot:EquipmentSlot.VALUES)if(p.getItemBySlot(slot).isEnchanted())observe(p,QuestTree.Fact.ENCHANTED,1);
        observe(p,QuestTree.Fact.XP_LEVEL,p.experienceLevel);
        if(p.level().dimension().equals(Level.NETHER))observe(p,QuestTree.Fact.NETHER_VISIT,1);
        if(p.level().dimension().equals(Level.OVERWORLD)&&fact(p,QuestTree.Fact.NETHER_VISIT)>0)observe(p,QuestTree.Fact.NETHER_RETURN,1);
        if(CampaignRules.era(p)==6){if(p.level().dimension().equals(Level.END))observe(p,QuestTree.Fact.END_VISIT,1);var end=p.level().getServer().getLevel(Level.END);if(end!=null&&end.getDragonFight()!=null&&end.getDragonFight().hasPreviouslyKilledDragon())observe(p,QuestTree.Fact.DRAGON,1);if(p.level().dimension().equals(Level.OVERWORLD)&&fact(p,QuestTree.Fact.END_VISIT)>0&&fact(p,QuestTree.Fact.DRAGON)>0)observe(p,QuestTree.Fact.END_RETURN,1);}
    }
    private static void nearbyFacts(ServerPlayer p){
        int era=CampaignRules.era(p);boolean needCircuit=fact(p,QuestTree.Fact.CIRCUIT)==0,needPiston=era>=4&&fact(p,QuestTree.Fact.EXTENDED_PISTON)==0,needFarm=era==5&&fact(p,QuestTree.Fact.FARMLAND)<16,needPortal=era==6&&fact(p,QuestTree.Fact.PORTAL_ROOM)==0;
        if(!needCircuit&&!needPiston&&!needFarm&&!needPortal)return;int farms=0,frames=0;var origin=p.blockPosition();
        for(var pos:net.minecraft.core.BlockPos.betweenClosed(origin.offset(-5,-3,-5),origin.offset(5,3,5))){if(!p.level().hasChunkAt(pos))continue;var block=p.level().getBlockState(pos);
            if(needCircuit&&block.is(Blocks.REDSTONE_WIRE)&&block.getValue(BlockStateProperties.POWER)>0)observe(p,QuestTree.Fact.CIRCUIT,1);
            if(needPiston&&(block.is(Blocks.PISTON)||block.is(Blocks.STICKY_PISTON))&&block.getValue(BlockStateProperties.EXTENDED))observe(p,QuestTree.Fact.EXTENDED_PISTON,1);
            if(block.is(Blocks.FARMLAND))farms++;if(block.is(Blocks.END_PORTAL_FRAME))frames++;
        }
        if(needFarm)observe(p,QuestTree.Fact.FARMLAND,farms);if(needPortal&&frames>=12)observe(p,QuestTree.Fact.PORTAL_ROOM,1);
    }
    private static void award(ServerPlayer p,String suffix,String criterion){var holder=p.level().getServer().getAdvancements().get(net.minecraft.resources.Identifier.fromNamespaceAndPath("betaminecraftmod",QuestChapter.get(CampaignRules.era(p)).path()+"/"+suffix));if(holder==null)return;var progress=p.getAdvancements().getOrStartProgress(holder).getCriterion(criterion);if(progress!=null&&!progress.isDone())p.getAdvancements().award(holder,criterion);}
    private static void mirror(ServerPlayer p,Set<String> complete){if(complete.contains("intro"))award(p,"root","completed");for(var q:QuestTree.goals(CampaignRules.era(p)))if(complete.contains(q.id()))award(p,"tree_"+q.id(),"completed");
        for(String branch:new String[]{"prep","explore","combat","build"})if(QuestTree.goals(CampaignRules.era(p)).stream().filter(q->q.branch().equals(branch)&&!q.optional()).allMatch(q->complete.contains(q.id())))award(p,"cerco",branch);
        if(CampaignSiege.won(p.level(),CampaignRules.era(p)))award(p,"cerco","victory");
    }
    public static void sync(ServerPlayer p,boolean force){if(!active(p))return;var status=CampaignSiege.snapshot(p);var payload=new TreeQuestStatePayload(CampaignRules.era(p),QuestTree.mask(CampaignRules.era(p),completed(p)),CampaignSiege.won(p.level(),CampaignRules.era(p)),status[0],status[1],status[2],status[3],CampaignProgress.get(p.level()).canUnlock(p.level()));var previous=SENT.put(p,payload);if(force||!payload.equals(previous))PacketDistributor.sendToPlayer(p,payload);}
    public static void action(ServerPlayer p,QuestActionPayload request){if(!active(p)||request.era()!=CampaignRules.era(p))return;
        if(!request.claim()){observe(p,QuestTree.Fact.OPEN,1);refresh(p);sync(p,true);return;}
        if(request.expectedStage()==-12){CampaignSiege.abort(p);}
        else if(request.expectedStage()==-10){if(ready(p))CampaignSiege.start(p);else p.sendSystemMessage(Component.literal("Complete os quatro ramos antes de iniciar o cerco."));}
        else if(request.expectedStage()==-11){if(!CampaignSiege.won(p.level(),CampaignRules.era(p))){p.sendSystemMessage(Component.literal("Vença o cerco antes de avançar."));return;}if(CampaignSiege.running(p.level()))return;
            if(!CampaignProgress.get(p.level()).unlockRing(p.level()))p.sendSystemMessage(Component.literal(CampaignRules.era(p)==5&&EraCatalog.get(6).playable()?"Esta campanha antiga não tem a garantia do portal. Crie uma campanha nova para jogar o lançamento sem alterar terreno anterior.":"A próxima era ainda está em desenvolvimento. Você pode permanecer nesta era."));else for(var other:p.level().getServer().getPlayerList().getPlayers())if(active(other)){observe(other,QuestTree.Fact.OPEN,1);refresh(other);sync(other,true);}}
        sync(p,true);
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){if(!(event.getEntity() instanceof ServerPlayer p)||!active(p))return;int era=CampaignRules.era(p);var before=POSITIONS.put(p,new Position(era,p.getX(),p.getZ()));if(before!=null&&before.era==era&&!p.isPassenger()&&p.onGround()){double d=Math.hypot(p.getX()-before.x,p.getZ()-before.z);if(d<=1.5){var t=data(p);String k=field(p,"travel_fraction");double total=t.getDoubleOr(k,0)+d;observe(p,QuestTree.Fact.TRAVEL,fact(p,QuestTree.Fact.TRAVEL)+(int)total);t=data(p);t.putDouble(k,total-Math.floor(total));p.getPersistentData().put(KEY,t);}}
        if(p.tickCount%20==0)refresh(p);
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent event){if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer p)||!active(p))return;if(event.getPlacedBlock().getBlock() instanceof net.minecraft.world.level.block.BedBlock)observe(p,QuestTree.Fact.BED,1);if(!event.getPlacedBlock().isSolidRender())return;observe(p,QuestTree.Fact.PLACED,fact(p,QuestTree.Fact.PLACED)+1);if(p.level().dimension().equals(Level.NETHER))observe(p,QuestTree.Fact.NETHER_BUILD,fact(p,QuestTree.Fact.NETHER_BUILD)+1);}
    @SubscribeEvent public static void killed(LivingDeathEvent event){if(event.isCanceled()||CampaignSiege.tagged(event.getEntity())||!(event.getSource().getEntity() instanceof ServerPlayer p)||!active(p))return;QuestTree.Fact fact=event.getEntity().getType()==net.minecraft.world.entity.EntityTypes.ZOMBIE?QuestTree.Fact.KILLS:event.getEntity().getType()==net.minecraft.world.entity.EntityTypes.GHAST?QuestTree.Fact.GHAST_KILLS:event.getEntity().getType()==net.minecraft.world.entity.EntityTypes.SKELETON?QuestTree.Fact.SKELETON_KILLS:event.getEntity().getType()==net.minecraft.world.entity.EntityTypes.SPIDER?QuestTree.Fact.SPIDER_KILLS:event.getEntity().getType()==net.minecraft.world.entity.EntityTypes.ENDERMAN?QuestTree.Fact.ENDERMAN_KILLS:event.getEntity().getType()==net.minecraft.world.entity.EntityTypes.BLAZE?QuestTree.Fact.BLAZE_KILLS:null;if(fact!=null)observe(p,fact,fact(p,fact)+1);}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void harvested(net.neoforged.neoforge.event.level.BlockDropsEvent event){if(event.isCanceled()||!(event.getBreaker() instanceof ServerPlayer p)||!active(p))return;var state=event.getState();if(state.is(Blocks.WHEAT)&&state.getValue(BlockStateProperties.AGE_7)==7)observe(p,QuestTree.Fact.HARVEST,fact(p,QuestTree.Fact.HARVEST)+1);}
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent event){if(event.getEntity() instanceof ServerPlayer p&&active(p)&&event.getCrafting().is(Items.BREAD))observe(p,QuestTree.Fact.CRAFTED_BREAD,fact(p,QuestTree.Fact.CRAFTED_BREAD)+event.getCrafting().getCount());}
    @SubscribeEvent public static void clone(PlayerEvent.Clone event){var t=event.getOriginal().getPersistentData().get(KEY);if(t!=null)event.getEntity().getPersistentData().put(KEY,t.copy());}
    @SubscribeEvent public static void joined(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer p&&active(p)){refresh(p);sync(p,true);}}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event){if(event.getEntity() instanceof ServerPlayer p&&active(p)){refresh(p);sync(p,true);}}
    private QuestTreeService(){}
}
