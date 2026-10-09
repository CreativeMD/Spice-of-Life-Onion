package team.creative.solonion.common.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
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
        if (player.level().isClientSide())
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

    public void onPlayerLogin(Player player) {
        updatePlayerBenefits(player);
        syncFoodList(player);
    }

    public void onPlayerDimensionChange(Player player, ResourceKey<Level> fromDim, ResourceKey<Level> toDim) {
        syncFoodList(player);
    }

    public void onClone(Player originalPlayer, Player newPlayer, boolean wasDeath) {
        if (wasDeath && SOLOnion.CONFIG.resetOnDeath)
            return;

        var provider = newPlayer.registryAccess();
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        var input = TagValueInput.create(ProblemReporter.DISCARDING, provider, output.buildResult());

        SOLOnionAPI.FOOD_DATA.set(newPlayer, FoodPlayerDataImpl.copy(SOLOnionAPI.getFoodCapability(originalPlayer)));
        
        BenefitPlayerDataImpl benefit = new BenefitPlayerDataImpl();
        output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        SOLOnionAPI.getBenefitCapability(originalPlayer).serialize(output);
        input = TagValueInput.create(ProblemReporter.DISCARDING, provider, output.buildResult());
        benefit.deserialize(input);

        SOLOnionAPI.BENEFIT_DATA.set(newPlayer, benefit);
    }

    public void onPlayerRespawn(Player player) {
        updatePlayerBenefits(player);
        syncFoodList(player);
    }
    
    public void syncFoodList(Player player) {
        if (player.level().isClientSide())
            return;
        
        SOLOnion.NETWORK.sendToClient(new FoodListMessage(player.registryAccess(), SOLOnionAPI.getFoodCapability(player)), (ServerPlayer) player);
    }

    public void onFoodEaten(LivingEntity entity, ItemStack usedItem) {
        if (!(entity instanceof Player player))
            return;

        if (!SOLOnion.isActive(player))
            return;

		if (usedItem.get(DataComponents.FOOD) == null && usedItem.getItem() != Items.CAKE)
            return;
        if (usedItem.getItem() instanceof FoodContainerItem)
            return;
        
        eat(usedItem, player);
    }
    
    public void eat(ItemStack food, Player player) {
        FoodPlayerData foodList = SOLOnionAPI.getFoodCapability(player);
        foodList.eat(player, food);
        updatePlayerBenefits(player);
        syncFoodList(player);
    }
    
}
