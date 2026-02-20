package team.creative.solonion.common.item.foodcontainer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import team.creative.creativecore.common.util.type.list.TupleList;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.OnionFoodContainer;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.mod.OriginsManager;

public class FoodContainerItem extends Item implements OnionFoodContainer {
    
    public static void setContainer(ItemStack stack, Container container) {
        List<ItemStack> stacks = new ArrayList<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); i++)
            stacks.add(container.getItem(i));
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
    }
    
    public static SimpleContainer getContainer(ItemStack stack) {
        if (stack.getItem() instanceof FoodContainerItem item) {
            var content = getInventory(stack);
            SimpleContainer container = new SimpleContainer(item.nslots);
            for (int i = 0; i < content.getSlots(); i++)
                container.setItem(i, content.getStackInSlot(i));
            return container;
        }
        return null;
    }
    
    private static boolean isInventoryEmpty(Player player, ItemStack container) {
        ItemStackHandler handler = getInventory(container);
        if (handler == null)
            return true;
        
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (isFoodItem(player, stack))
                return false;
        }
        return true;
    }
    
    private static void broadcastChangesOnContainerMenu(Player player) {
        if (player.containerMenu != null)
            player.containerMenu.slotsChanged(player.getInventory());
    }
    
    public static int getBestFoodSlot(ItemStackHandler handler, Player player) {
        FoodPlayerData foodList = SOLOnionAPI.getFoodCapability(player);
        
        double maxDiversity = -Double.MAX_VALUE;
        int bestFoodSlot = -1;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack food = handler.getStackInSlot(i);
            
            if (!isFoodItem(player, food))
                continue;
            
            double diversityChange = foodList.simulateEat(player, food);
            if (diversityChange > maxDiversity) {
                maxDiversity = diversityChange;
                bestFoodSlot = i;
            }
        }
        
        return bestFoodSlot;
    }
    
    public static boolean isFoodItem(Player player, ItemStack stack) {
        return !stack.isEmpty() && stack.getFoodProperties(player) != null && OriginsManager.isEdible(player, stack);
    }
    
    public static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
    
    public static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
    
    public static void playInsertFailSound(Entity entity) {
        //entity.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F); Not available in 1.21.1
    }
    
    @Nullable
    public static ItemStackHandler getInventory(ItemStack bag) {
        return (ItemStackHandler) bag.getCapability(Capabilities.ItemHandler.ITEM);
    }
    
    private String displayName;
    public final int nslots;
    
    public FoodContainerItem(int nslots, String displayName) {
        super(new Properties().stacksTo(1).setNoRepair().food(new FoodProperties.Builder().build()));
        
        this.displayName = displayName;
        this.nslots = nslots;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var handler = Capabilities.ItemHandler.BLOCK.getCapability(context.getLevel(), context.getClickedPos(), null, null, context.getClickedFace());
        if (handler == null)
            return super.useOn(context);
        
        ItemStackHandler inv = getInventory(context.getItemInHand());
        if (inv == null)
            return super.useOn(context);
        TupleList<Double, Integer> bestStacks = new TupleList<Double, Integer>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getFoodProperties(context.getPlayer()) != null && OriginsManager.isEdible(context.getPlayer(), stack)) {
                for (int j = 0; j < inv.getSlots(); j++) { // Fill up the slots which are already taken
                    var toBeStacked = inv.getStackInSlot(j);
                    if (ItemStack.isSameItem(stack, toBeStacked) && ItemStack.isSameItemSameComponents(stack, toBeStacked)) {
                        int maxStackSize = Math.min(stack.getMaxStackSize(), inv.getSlotLimit(j));
                        if (!toBeStacked.isEmpty() && toBeStacked.getCount() < maxStackSize)
                            toBeStacked.grow(handler.extractItem(i, maxStackSize - toBeStacked.getCount(), false).getCount());
                        stack = handler.getStackInSlot(i);
                        if (stack.isEmpty())
                            break;
                    }
                }
                bestStacks.add(SOLOnion.CONFIG.getDiversity(context.getPlayer(), stack), i);
            }
        }
        
        bestStacks.sort(Comparator.comparingDouble(x -> x.key));
        
        for (int slot : bestStacks.values()) {
            var stack = handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
            var result = ItemHandlerHelper.insertItem(inv, stack, false);
            if (!result.isEmpty()) {
                handler.insertItem(slot, result, false);
                break;
            }
        }
        
        List<ItemStack> stacks = new ArrayList<>(inv.getSlots());
        for (int i = 0; i < inv.getSlots(); i++)
            stacks.add(inv.getStackInSlot(i));
        context.getItemInHand().set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
        
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide && player.isCrouching())
            player.openMenu(new FoodContainerProvider(displayName), player.blockPosition());
        
        if (!player.isCrouching())
            return processRightClick(world, player, hand);
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
    
    private InteractionResultHolder<ItemStack> processRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isInventoryEmpty(player, stack))
            return InteractionResultHolder.pass(stack);
        
        if (player.canEat(false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item." + SOLOnion.MODID + ".container.open", Component.keybind("key.sneak"), Component.keybind("key.use")));
        super.appendHoverText(stack, context, tooltip, flag);
    }
    
    @Override
    public ItemStack getActualFood(Player player, ItemStack stack) {
        ItemStackHandler handler = getInventory(stack);
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
        ItemStackHandler handler = getInventory(stack);
        if (handler == null)
            return stack;
        
        int bestFoodSlot = getBestFoodSlot(handler, player);
        if (bestFoodSlot < 0)
            return stack;
        
        ItemStack bestFood = handler.getStackInSlot(bestFoodSlot);
        ItemStack foodCopy = bestFood.copy();
        if (isFoodItem(player, bestFood)) {
            ItemStack result = bestFood.finishUsingItem(world, entity);
            // put bowls/bottles etc. into player inventory
            if (result.getFoodProperties(player) == null) {
                handler.setStackInSlot(bestFoodSlot, ItemStack.EMPTY);
                Player playerEntity = (Player) entity;
                
                if (!playerEntity.getInventory().add(result))
                    playerEntity.drop(result, false);
                
            }
            
            List<ItemStack> stacks = new ArrayList<>(handler.getSlots());
            for (int i = 0; i < handler.getSlots(); i++)
                stacks.add(handler.getStackInSlot(i));
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
            
            if (!world.isClientSide)
                EventHooks.onItemUseFinish(player, foodCopy, 0, result);
        }
        
        return stack;
    }
    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStackHandler handler = getInventory(stack);
        if (handler == null || !(entity instanceof Player))
            return 32;
        
        int bestFoodSlot = getBestFoodSlot(handler, (Player) entity);
        if (bestFoodSlot < 0)
            return 32;
        return handler.getStackInSlot(bestFoodSlot).getUseDuration(entity);
    }
    
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        var container = getContainer(stack);
        ItemStack itemstack = slot.getItem();
        if (action == ClickAction.PRIMARY && !itemstack.isEmpty()) {
            if (isFoodItem(player, itemstack) && container.canAddItem(itemstack)) {
                slot.set(container.addItem(itemstack));
                playInsertSound(player);
                setContainer(stack, container);
                broadcastChangesOnContainerMenu(player);
            } else
                playInsertFailSound(player);
            return true;
        }
        
        if (action == ClickAction.SECONDARY && itemstack.isEmpty()) {
            ItemStack toRemove = ItemStack.EMPTY;
            for (int i = 0; i < container.getContainerSize(); i++)
                if (!container.getItem(i).isEmpty()) {
                    toRemove = container.removeItemNoUpdate(i);
                    break;
                }
            
            if (toRemove.isEmpty())
                return true;
            
            ItemStack remaining = slot.safeInsert(toRemove);
            if (!remaining.isEmpty())
                container.addItem(remaining);
            else
                playRemoveOneSound(player);
            
            setContainer(stack, container);
            broadcastChangesOnContainerMenu(player);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess hand) {
        if (stack.getCount() != 1)
            return false;
        
        if (action == ClickAction.PRIMARY && other.isEmpty())
            return false;
        
        var container = getContainer(stack);
        if (action == ClickAction.PRIMARY && !other.isEmpty()) {
            if (isFoodItem(player, other) && container.canAddItem(other)) {
                hand.set(container.addItem(other));
                playInsertSound(player);
                setContainer(stack, container);
                broadcastChangesOnContainerMenu(player);
            } else
                playInsertFailSound(player);
            return true;
        }
        
        if (action == ClickAction.SECONDARY && other.isEmpty()) {
            if (slot.allowModification(player)) {
                ItemStack toRemove = ItemStack.EMPTY;
                for (int i = 0; i < container.getContainerSize(); i++)
                    if (!container.getItem(i).isEmpty()) {
                        toRemove = container.removeItemNoUpdate(i);
                        break;
                    }
                
                if (toRemove.isEmpty())
                    return true;
                
                playRemoveOneSound(player);
                hand.set(toRemove);
                
                setContainer(stack, container);
                broadcastChangesOnContainerMenu(player);
            }
            return true;
        }
        
        return false;
    }
    
}
