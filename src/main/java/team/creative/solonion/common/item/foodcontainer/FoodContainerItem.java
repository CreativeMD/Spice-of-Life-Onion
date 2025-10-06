package team.creative.solonion.common.item.foodcontainer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import team.creative.creativecore.common.util.inventory.InventoryUtils;
import team.creative.creativecore.common.util.type.list.TupleList;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.OnionFoodContainer;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.mod.OriginsManager;

public class FoodContainerItem extends Item implements OnionFoodContainer {
    
    private String displayName;
    public final int nslots;
    
    public FoodContainerItem(Properties p, int nslots, String displayName) {
        super(p.useItemDescriptionPrefix().stacksTo(1).food(new FoodProperties.Builder().build()));
        
        this.displayName = displayName;
        this.nslots = nslots;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var handler = Capabilities.Item.BLOCK.getCapability(context.getLevel(), context.getClickedPos(), null, null, context.getClickedFace());
        if (handler == null)
            return super.useOn(context);
        
        ResourceHandler<ItemResource> inv = context.getItemInHand().getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(context.getItemInHand()));
        TupleList<Double, Integer> bestStacks = new TupleList<Double, Integer>();
        for (int i = 0; i < handler.size(); i++) {
            ItemResource resource = handler.getResource(i);
            if (!resource.isEmpty() && resource.get(DataComponents.FOOD) != null && OriginsManager.isEdible(context.getPlayer(), resource.toStack(handler.getAmountAsInt(i)))) {
                
                for (int j = 0; j < inv.size(); j++) { // Fill up the slots which are already taken
                    var toBeStacked = inv.getResource(j);
                    
                    if (resource.is(toBeStacked.getItem()) && resource.getComponents().equals(toBeStacked.getComponents())) {
                        int maxStackSize = Math.min(resource.getMaxStackSize(), inv.getCapacityAsInt(j, toBeStacked));
                        if (!toBeStacked.isEmpty() && inv.getAmountAsInt(j) < maxStackSize) {
                            try (var tx = Transaction.open(null)) {
                                inv.insert(j, toBeStacked, handler.extract(i, resource, maxStackSize - inv.getAmountAsInt(j), tx), tx);
                                tx.commit();
                            }
                            
                            if (handler.getAmountAsInt(i) <= 0)
                                break;
                        }
                    }
                }
                
                bestStacks.add(SOLOnion.CONFIG.getDiversity(context.getPlayer(), resource.toStack(handler.getAmountAsInt(i))), i);
            }
        }
        
        bestStacks.sort(Comparator.comparingDouble(x -> x.key));
        
        for (int slot : bestStacks.values()) {
            try (var tx = Transaction.open(null)) {
                var resource = handler.getResource(slot);
                var stack = handler.extract(slot, resource, handler.getAmountAsInt(slot), tx);
                stack -= inv.insert(resource, stack, tx);
                if (stack > 0) {
                    handler.insert(resource, stack, tx);
                    break;
                }
                tx.commit();
            }
        }
        
        context.getItemInHand().set(DataComponents.CONTAINER, InventoryUtils.asContent(inv));
        
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide() && player.isCrouching())
            player.openMenu(new FoodContainerProvider(displayName), player.blockPosition());
        
        if (!player.isCrouching())
            return processRightClick(world, player, hand);
        return InteractionResult.PASS;
    }
    
    private InteractionResult processRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isInventoryEmpty(player, stack))
            return InteractionResult.PASS;
        
        if (player.canEat(false)) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }
    
    private static boolean isInventoryEmpty(Player player, ItemStack container) {
        ItemContainerContents handler = getInventory(container);
        if (handler == null)
            return true;
        
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.get(DataComponents.FOOD) != null && OriginsManager.isEdible(player, stack))
                return false;
        }
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item." + SOLOnion.MODID + ".container.open", Component.keybind("key.sneak"), Component.keybind("key.use")));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
    
    @Nullable
    public static ItemContainerContents getInventory(ItemStack bag) {
        if (bag.getItem() instanceof FoodContainerItem item) {
            if (bag.has(DataComponents.CONTAINER))
                return bag.get(DataComponents.CONTAINER);
            List<ItemStack> stacks = new ArrayList<>(item.nslots);
            for (int i = 0; i < item.nslots; i++)
                stacks.add(ItemStack.EMPTY);
            ItemContainerContents contents = ItemContainerContents.fromItems(stacks);
            bag.set(DataComponents.CONTAINER, contents);
            return contents;
        }
        return null;
    }
    
    @Override
    public ItemStack getActualFood(Player player, ItemStack stack) {
        ItemContainerContents handler = getInventory(stack);
        if (handler == null)
            return ItemStack.EMPTY;
        
        int bestFoodSlot = getBestFoodSlot(handler, player);
        if (bestFoodSlot < 0)
            return ItemStack.EMPTY;
        return handler.getStackInSlot(bestFoodSlot).copy();
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!(entity instanceof Player))
            return stack;
        
        Player player = (Player) entity;
        ItemContainerContents handler = getInventory(stack);
        if (handler == null)
            return stack;
        
        int bestFoodSlot = getBestFoodSlot(handler, player);
        if (bestFoodSlot < 0)
            return stack;
        
        NonNullList<ItemStack> newInventory = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        handler.copyInto(newInventory);
        ItemStack bestFood = handler.getStackInSlot(bestFoodSlot);
        ItemStack foodCopy = bestFood.copy();
        if (bestFood.get(DataComponents.FOOD) != null && !bestFood.isEmpty() && OriginsManager.isEdible(player, foodCopy)) {
            ItemStack result = bestFood.finishUsingItem(world, entity);
            newInventory.set(bestFoodSlot, result);
            // put bowls/bottles etc. into player inventory
            if (result.get(DataComponents.FOOD) == null) {
                newInventory.set(bestFoodSlot, ItemStack.EMPTY);
                Player playerEntity = (Player) entity;
                
                if (!playerEntity.getInventory().add(result))
                    playerEntity.drop(result, false);
                
            }
            
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(newInventory));
            
            if (!world.isClientSide())
                EventHooks.onItemUseFinish(player, foodCopy, 0, result);
        }
        
        return stack;
    }
    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }
    
    public static int getBestFoodSlot(ItemContainerContents handler, Player player) {
        FoodPlayerData foodList = SOLOnionAPI.getFoodCapability(player);
        
        double maxDiversity = -Double.MAX_VALUE;
        int bestFoodSlot = -1;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack food = handler.getStackInSlot(i);
            
            if (food.get(DataComponents.FOOD) == null || food.isEmpty() || !OriginsManager.isEdible(player, food))
                continue;
            
            double diversityChange = foodList.simulateEat(player, food);
            if (diversityChange > maxDiversity) {
                maxDiversity = diversityChange;
                bestFoodSlot = i;
            }
        }
        
        return bestFoodSlot;
    }
}
