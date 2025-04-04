package team.creative.solonion.common.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.benefit.BenefitPlayerDataImpl;
import team.creative.solonion.common.benefit.BenefitStack;
import team.creative.solonion.common.benefit.BenefitThreshold;
import team.creative.solonion.common.food.FoodPlayerDataImpl;
import team.creative.solonion.common.item.foodcontainer.FoodContainerItem;
import team.creative.solonion.common.network.FoodListMessage;

public class SOLOnionEvent {
    
    public void updatePlayerBenefits(Player player) {
        if (!SOLOnion.isActive(player) || !player.isAlive())
            return;
        
        updateBenefits(player);
    }
    
    private void updateBenefits(Player player) {
        if (player.getCommandSenderWorld().isClientSide)
            return;
        
        FoodPlayerData foodList = SOLOnionAPI.getFoodCapability(player);
        if (foodList.trackCount() < SOLOnion.CONFIG.minFoodsToActivate)
            return;
        
        BenefitStack stack = new BenefitStack();
        double d = foodList.foodDiversity(player);
        for (BenefitThreshold threshold : SOLOnion.CONFIG.benefits) {
            if (threshold.threshold <= d)
                stack.add(threshold.benefit);
            else
                break;
        }
        
        for (BenefitThreshold threshold : SOLOnion.CONFIG.detriments) {
            if (threshold.threshold > d)
                stack.add(threshold.benefit);
            else
                break;
        }
        
        SOLOnionAPI.getBenefitCapability(player).updateStack(player, stack);
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        updatePlayerBenefits(event.getEntity());
        syncFoodList(event.getEntity());
    }
    
    @SubscribeEvent
    public void onPlayerDimensionChange(PlayerChangedDimensionEvent event) {
        syncFoodList(event.getEntity());
    }
    
    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && SOLOnion.CONFIG.resetOnDeath)
            return;
        
        Player originalPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        var provider = newPlayer.registryAccess();
        
        FoodPlayerDataImpl food = new FoodPlayerDataImpl();
        food.deserializeNBT(provider, SOLOnionAPI.getFoodCapability(originalPlayer).serializeNBT(provider));
        newPlayer.setData(SOLOnionAPI.FOOD_DATA, food);
        
        BenefitPlayerDataImpl benefit = new BenefitPlayerDataImpl();
        benefit.deserializeNBT(provider, SOLOnionAPI.getBenefitCapability(originalPlayer).serializeNBT(provider));
        newPlayer.setData(SOLOnionAPI.BENEFIT_DATA, benefit);
    }
    
    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        updatePlayerBenefits(event.getEntity());
        syncFoodList(event.getEntity());
    }
    
    public void syncFoodList(Player player) {
        if (player.level().isClientSide)
            return;
        
        SOLOnion.NETWORK.sendToClient(new FoodListMessage(player.registryAccess(), SOLOnionAPI.getFoodCapability(player)), (ServerPlayer) player);
    }
    
    @SubscribeEvent
    public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getEntity();
        if (!SOLOnion.isActive(player))
            return;
        
        ItemStack usedItem = event.getItem();
        if (usedItem.getFoodProperties(player) == null && usedItem.getItem() != Items.CAKE)
            return;
        if (usedItem.getItem() instanceof FoodContainerItem)
            return;
        
        eat(usedItem, player);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCakeBlockEaten(PlayerInteractEvent.RightClickBlock event) {
        // Canceled means some other mod already resolved this event,
        // e.g. Farmer's Delight cut off a slice with a knife.
        if (event.isCanceled())
            return;
        
        BlockState state = event.getLevel().getBlockState(event.getPos());
        Block clickedBlock = state.getBlock();
        Player player = event.getEntity();
        
        Item eatenItem = Items.CAKE;
        // If Farmer's Delight is installed, replace "cake" with FD's "cake slice"
        if (ModList.get().isLoaded("farmersdelight"))
            eatenItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryBuild("farmersdelight", "cake_slice"));
        ItemStack eatenItemStack = new ItemStack(eatenItem);
        
        if (clickedBlock == Blocks.CAKE && player.canEat(false) && event.getHand() == InteractionHand.MAIN_HAND && !event.getLevel().isClientSide) {
            // Fire an event instead of directly updating the food list, so that
            // SoL: Carrot Edition registers the eaten food too.
            EventHooks.onItemUseFinish(player, eatenItemStack, 0, ItemStack.EMPTY);
        }
    }
    
    public void eat(ItemStack food, Player player) {
        FoodPlayerData foodList = SOLOnionAPI.getFoodCapability(player);
        foodList.eat(player, food);
        updatePlayerBenefits(player);
        syncFoodList(player);
    }
    
}
