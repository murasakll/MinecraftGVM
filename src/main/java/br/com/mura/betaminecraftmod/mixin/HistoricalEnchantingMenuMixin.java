package br.com.mura.betaminecraftmod.mixin;

import br.com.mura.betaminecraftmod.progression.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(EnchantmentMenu.class)
public abstract class HistoricalEnchantingMenuMixin extends AbstractContainerMenu implements HistoricalEnchantingMenuAccess {
    @Shadow @Final private Container enchantSlots;
    @Shadow @Final private ContainerLevelAccess access;
    @Shadow @Final private DataSlot enchantmentSeed;
    @Shadow @Final public int[] costs;
    @Shadow @Final public int[] enchantClue;
    @Shadow @Final public int[] levelClue;
    @Unique private Player campaign$owner;
    @Unique private final Random campaign$enchantRandom=new Random();
    @Unique private final DataSlot[] campaign$labelParts={DataSlot.standalone(),DataSlot.standalone(),DataSlot.standalone(),DataSlot.standalone()};
    @Override public long campaign$labelSeed(){long seed=0;for(int i=0;i<4;i++)seed|=((long)campaign$labelParts[i].get()&65535L)<<(16*i);return seed;}
    protected HistoricalEnchantingMenuMixin(int id){super(MenuType.ENCHANTMENT,id);}
    @Inject(method="<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",at=@At("TAIL"))
    private void campaign$init(int id,Inventory inventory,ContainerLevelAccess access,CallbackInfo ci) {
        campaign$owner=inventory.player;ReleaseEnchanting.register(enchantSlots,campaign$owner);
        // Four 16-bit pieces also survive the vanilla container-data transport's signed-short encoding.
        for(var part:campaign$labelParts)addDataSlot(part);
        if(ReleaseBrewing.active(campaign$owner.level()))((SlotPositionAccessor)getSlot(0)).campaign$setX(25);
    }
    @Inject(method="slotsChanged",at=@At("HEAD"),cancellable=true)
    private void campaign$offers(Container container,CallbackInfo ci) {
        if(campaign$owner==null||!ReleaseBrewing.active(campaign$owner.level()))return;
        ci.cancel();if(container!=enchantSlots||campaign$owner.level().isClientSide())return;
        Arrays.fill(enchantClue,-1);Arrays.fill(levelClue,-1);
        var stack=enchantSlots.getItem(0);
        if(!ReleaseEnchanting.accepts(stack)){Arrays.fill(costs,0);broadcastChanges();return;}
        long labelSeed=campaign$enchantRandom.nextLong();enchantmentSeed.set((int)labelSeed);
        for(int i=0;i<4;i++)campaign$labelParts[i].set((int)(labelSeed>>>(16*i))&65535);
        access.execute((level,pos)->{
            int books=ReleaseEnchanting.books(level,pos),value=ReleaseEnchantingCatalog.item(ReleaseEnchanting.key(stack)).enchantability();
            for(int row=0;row<3;row++)costs[row]=ReleaseEnchantingRules.cost(campaign$enchantRandom,row,books,value);
            broadcastChanges();
        });
    }
    @Inject(method="clickMenuButton",at=@At("HEAD"),cancellable=true)
    private void campaign$enchant(Player player,int row,CallbackInfoReturnable<Boolean> result) {
        if(!ReleaseBrewing.active(player.level()))return;
        var stack=enchantSlots.getItem(0);
        if(row<0||row>=3||costs[row]<=0||!ReleaseEnchanting.accepts(stack)||player.experienceLevel<costs[row]){result.setReturnValue(false);return;}
        result.setReturnValue(true);if(player.level().isClientSide())return;
        access.execute((level,pos)->{
            var selected=ReleaseEnchantingRules.select(campaign$enchantRandom,ReleaseEnchanting.key(stack),costs[row]);
            if(selected.isEmpty())return;
            int cost=costs[row];player.giveExperienceLevels(-cost);
            var registry=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);var applied=new ArrayList<EnchantmentInstance>();
            for(var entry:selected){var holder=registry.getOrThrow(net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT,Identifier.parse(entry.key())));stack.enchant(holder,entry.level());applied.add(new EnchantmentInstance(holder,entry.level()));}
            net.neoforged.neoforge.common.CommonHooks.onPlayerEnchantItem(player,stack,applied);
            enchantSlots.setChanged();
            // Keep the host audio event while historical audio work remains on hold.
            level.playSound(null,pos,net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,net.minecraft.sounds.SoundSource.BLOCKS,1F,level.getRandom().nextFloat()*.1F+.9F);
        });
    }
    @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
    private void campaign$noInputShift(Player player,int index,CallbackInfoReturnable<ItemStack> result) {
        if(ReleaseBrewing.active(player.level())&&index!=0)result.setReturnValue(ItemStack.EMPTY);
    }
}
