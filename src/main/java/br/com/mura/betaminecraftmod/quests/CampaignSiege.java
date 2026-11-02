package br.com.mura.betaminecraftmod.quests;

import br.com.mura.betaminecraftmod.progression.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.*;

/** One explicitly started Overworld siege per campaign. Running attempts abort on restart. */
@EventBusSubscriber(modid="betaminecraftmod")
public final class CampaignSiege {
    public static final String TAG="betaminecraftmod:siege_attempt";
    public static final class Wins extends SavedData {
        public static final SavedDataType<Wins> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("betaminecraftmod","siege_wins"),()->new Wins(0),Codec.INT.optionalFieldOf("won_mask",0).xmap(Wins::new,w->w.mask).codec());
        private int mask;
        private Wins(int mask){this.mask=mask;}
        public boolean won(int era){return era>=0&&era<20&&(mask&(1<<era))!=0;}
        private void award(int era){mask|=1<<era;setDirty();}
    }
    private static final class Attempt {
        final UUID owner,nonce=UUID.randomUUID();final int era;final BlockPos anchor;final QuestTree.SiegePlan plan;
        final Set<UUID> alive=new HashSet<>(),deathCandidates=new HashSet<>();
        final ServerBossEvent bar=new ServerBossEvent(UUID.randomUUID(),Component.literal("Cerco"),net.minecraft.world.BossEvent.BossBarColor.RED,net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
        int wave=1,pending,wait=100,age,stalled;
        Attempt(ServerPlayer p){owner=p.getUUID();era=CampaignRules.era(p);anchor=p.blockPosition();plan=QuestTree.siege(era);pending=plan.waveSize(0);bar.addPlayer(p);}
    }
    private static final Map<MinecraftServer,Attempt> RUNNING=new WeakHashMap<>();
    private static Wins wins(ServerLevel level){return level.getServer().overworld().getDataStorage().computeIfAbsent(Wins.TYPE);}
    public static boolean won(ServerLevel level,int era){return wins(level).won(era);}
    public static boolean running(ServerLevel level){return RUNNING.containsKey(level.getServer());}
    public static boolean tagged(Entity e){return e.getPersistentData().contains(TAG);}
    public static int[] snapshot(ServerPlayer p){var a=RUNNING.get(p.level().getServer());return a==null?new int[]{0,0,0,0}:new int[]{a.owner.equals(p.getUUID())?(a.wait>0?1:2):3,a.wave,a.alive.size(),a.pending};}
    public static boolean abort(ServerPlayer p){var a=RUNNING.get(p.level().getServer());if(a==null||!a.owner.equals(p.getUUID()))return false;finish(p.level().getServer().overworld(),a,false,"tentativa encerrada por você.");return true;}
    public static boolean start(ServerPlayer p){
        if(!QuestTreeService.active(p)||!QuestTreeService.ready(p)||won(p.level(),CampaignRules.era(p))||running(p.level()))return false;
        if(!p.level().dimension().equals(Level.OVERWORLD)||!p.isAlive()||p.isSpectator()){message(p,"Inicie o cerco vivo, no Overworld.");return false;}
        if(p.level().getDifficulty()==Difficulty.PEACEFUL){message(p,"A horda exige Fácil, Normal ou Difícil. O mod não altera sua dificuldade automaticamente.");return false;}
        var a=new Attempt(p);RUNNING.put(p.level().getServer(),a);message(p,"Cerco preparado: "+a.plan.waves()+" ondas, "+a.plan.total()+" zumbis. Começa em 5 segundos. Fique a até 64 blocos do local inicial.");QuestTreeService.sync(p,true);return true;
    }
    private static void message(ServerPlayer p,String text){p.sendSystemMessage(Component.literal(text));}
    private static void cleanup(ServerLevel level,Attempt a){a.bar.removeAllPlayers();var entities=new ArrayList<Entity>();for(var entity:level.getAllEntities())if(entity.getPersistentData().getStringOr(TAG,"").equals(a.nonce.toString()))entities.add(entity);for(var entity:entities)entity.discard();}
    private static void finish(ServerLevel level,Attempt a,boolean victory,String why){RUNNING.remove(level.getServer());cleanup(level,a);if(victory)wins(level).award(a.era);var owner=level.getServer().getPlayerList().getPlayer(a.owner);if(owner!=null)message(owner,victory?"Cerco vencido! Abra os objetivos para permanecer nesta era ou confirmar o avanço.":"Tentativa encerrada: "+why+" Suas quests foram preservadas; você pode tentar novamente.");for(var p:level.getServer().getPlayerList().getPlayers())if(QuestTreeService.active(p)){QuestTreeService.refresh(p);QuestTreeService.sync(p,true);}}
    @SubscribeEvent public static void tick(LevelTickEvent.Post event){if(!(event.getLevel() instanceof ServerLevel level)||!level.dimension().equals(Level.OVERWORLD))return;var a=RUNNING.get(level.getServer());if(a==null)return;var p=level.getServer().getPlayerList().getPlayer(a.owner);
        if(p==null||!p.isAlive()||p.isSpectator()||p.level()!=level||CampaignRules.era(p)!=a.era||level.getDifficulty()==Difficulty.PEACEFUL||p.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(a.anchor))>64*64||++a.age>24000){finish(level,a,false,"participante ausente, fora da área, dificuldade alterada ou tempo esgotado.");return;}
        for(var id:new HashSet<>(a.alive)){var mob=level.getEntity(id);if(a.deathCandidates.contains(id)&&mob!=null&&!mob.isAlive()){a.alive.remove(id);a.deathCandidates.remove(id);}else if(mob==null||mob.isRemoved()){finish(level,a,false,"um inimigo deixou de estar disponível.");return;}else if(mob.isAlive()){a.deathCandidates.remove(id);if(mob instanceof Mob target){target.setTarget(p);if(a.age%20==0)target.getNavigation().moveTo(p,1);}}}
        if(a.age%20==0){int defeated=0;for(int i=0;i<a.wave;i++)defeated+=a.plan.waveSize(i);defeated-=a.pending+a.alive.size();a.bar.setProgress(1-defeated/(float)a.plan.total());a.bar.setName(Component.literal("Cerco · onda "+a.wave+"/"+a.plan.waves()+(a.wait>0?" · começa em "+((a.wait+19)/20)+"s":" · "+a.alive.size()+" zumbis ativos")));for(var other:level.players())if(QuestTreeService.active(other))QuestTreeService.sync(other,false);}
        if(a.wait>0){a.wait--;return;}
        if(a.age%10==0&&a.pending>0&&a.alive.size()<a.plan.concurrent()){
            if(spawn(level,p,a)){a.pending--;a.stalled=0;}else a.stalled+=10;
            if(a.stalled>=1200){finish(level,a,false,"não encontramos um local seguro para a horda. Tente uma área mais aberta.");return;}
        }
        if(a.pending==0&&a.alive.isEmpty()){if(a.wave==a.plan.waves()){finish(level,a,true,"");return;}a.wave++;a.pending=a.plan.waveSize(a.wave-1);a.wait=a.plan.restTicks();message(p,"Onda concluída. Próxima onda: "+a.wave+" / "+a.plan.waves()+".");}
        if(a.age%20==0)for(var other:level.players())if(QuestTreeService.active(other))QuestTreeService.sync(other,false);
    }
    private static boolean spawn(ServerLevel level,ServerPlayer player,Attempt a){for(int trial=0;trial<24;trial++){
        double angle=level.getRandom().nextDouble()*Math.PI*2,radius=20+level.getRandom().nextInt(15);int x=a.anchor.getX()+(int)(Math.cos(angle)*radius),z=a.anchor.getZ()+(int)(Math.sin(angle)*radius);var column=new BlockPos(x,0,z);
        if(!level.hasChunkAt(column)||!level.getWorldBorder().isWithinBounds(column))continue;
        int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);var pos=new BlockPos(x,y,z);
        if(!level.getBlockState(pos.below()).isSolidRender()||!level.getBlockState(pos).isAir()||!level.getBlockState(pos.above()).isAir()||level.getNearestPlayer(x+.5,y,z+.5,10,false)!=null)continue;
        var mob=net.minecraft.world.entity.EntityTypes.ZOMBIE.create(level,EntitySpawnReason.EVENT);if(mob==null)continue;mob.snapTo(x+.5,y,z+.5,0,0);if(!level.noCollision(mob)||level.containsAnyLiquid(mob.getBoundingBox())){mob.discard();continue;}
        mob.getPersistentData().putString(TAG,a.nonce.toString());mob.setPersistenceRequired();mob.setCanPickUpLoot(false);mob.setTarget(player);
        var follow=mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);if(follow!=null)follow.setBaseValue(64);
        var reinforcement=mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SPAWN_REINFORCEMENTS_CHANCE);if(reinforcement!=null)reinforcement.setBaseValue(0);
        if(level.addFreshEntity(mob)){a.alive.add(mob.getUUID());return true;}mob.discard();
    }return false;}
    @SubscribeEvent public static void death(LivingDeathEvent event){if(event.isCanceled()||!(event.getEntity().level() instanceof ServerLevel level)||!tagged(event.getEntity()))return;var a=RUNNING.get(level.getServer());if(a!=null&&a.alive.contains(event.getEntity().getUUID()))a.deathCandidates.add(event.getEntity().getUUID());}
    @SubscribeEvent public static void joined(EntityJoinLevelEvent event){if(event.getLevel().isClientSide()||!tagged(event.getEntity()))return;var a=RUNNING.get(((ServerLevel)event.getLevel()).getServer());if(a==null||!event.getEntity().getPersistentData().getStringOr(TAG,"").equals(a.nonce.toString())){event.setCanceled(true);event.getEntity().discard();}}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){var a=RUNNING.get(p.level().getServer());if(a!=null&&a.owner.equals(p.getUUID()))finish(p.level().getServer().overworld(),a,false,"jogador desconectou.");}}
    @SubscribeEvent public static void stopped(ServerStoppingEvent event){var a=RUNNING.remove(event.getServer());if(a!=null)cleanup(event.getServer().overworld(),a);}
    private CampaignSiege(){}
}
