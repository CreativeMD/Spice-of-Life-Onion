package team.creative.solonion.common.food;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.UnknownNullability;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.type.itr.ArrayOffsetIterator;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.list.TupleList;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.common.SOLOnion;

public final class FoodPlayerDataImpl implements FoodPlayerData {
    
    private static double calculateDiversity(Iterable<ItemStack> stacks, LivingEntity entity) {
        Object2DoubleArrayMap<Item> types = new Object2DoubleArrayMap<>();
        int i = 0;
        for (ItemStack stack : stacks) {
            double d = calculateDiversity(entity, stack, i);
            types.computeDouble(stack.getItem(), (x, y) -> {
                if (y == null)
                    return d;
                return Math.max(y, d);
            });
            i++;
        }
        double d = 0;
        for (Entry<Item> entry : types.object2DoubleEntrySet())
            d += entry.getDoubleValue();
        return d;
    }
    
    public static TupleList<ItemStack, Double> calculateDiversityIndividualy(Iterable<ItemStack> stacks, LivingEntity entity) {
        TupleList<ItemStack, Double> results = new TupleList<>();
        HashMap<Item, Tuple<ItemStack, Double>> types = new HashMap<>();
        int i = 0;
        for (ItemStack stack : stacks) {
            double d = calculateDiversity(entity, stack, i);
            Tuple<ItemStack, Double> existing = types.get(stack.getItem());
            boolean overwrite = false;
            if (existing != null)
                if (existing.value >= d) // If the other one is greater this one does not count
                    d = 0;
                else { // If this one is greater the other one does not count
                    existing.value = 0D;
                    overwrite = true;
                }
            else
                overwrite = true;
            
            Tuple<ItemStack, Double> tuple = new Tuple<>(stack, d);
            results.add(tuple);
            if (overwrite)
                types.put(stack.getItem(), tuple);
            i++;
        }
        return results;
    }
    
    public static double calculateDiversity(LivingEntity entity, ItemStack stack, int index) {
        return SOLOnion.CONFIG.getDiversity(entity, stack) * (1D - (index / (SOLOnion.CONFIG.trackCount + 1D)));
    }
    
    private ItemStack[] lastEaten = new ItemStack[SOLOnion.CONFIG.trackCount];
    private int startIndex = lastEaten.length - 1;
    private double diversityCache = -1;
    
    public FoodPlayerDataImpl() {}
    
    private void updateDiversity(LivingEntity entity) {
        diversityCache = calculateDiversity(this, entity);
    }
    
    @Override
    public @UnknownNullability ListTag serializeNBT(Provider provider) {
        ListTag list = new ListTag();
        for (ItemStack stack : this)
            list.add(stack.save(provider));
        
        return list;
    }
    
    @Override
    public void deserializeNBT(Provider provider, ListTag tag) {
        if (tag == null)
            return;
        for (int i = 0; i < lastEaten.length; i++)
            lastEaten[i] = i < tag.size() ? ItemStack.parse(provider, tag.getCompoundOrEmpty(i)).orElse(ItemStack.EMPTY) : null;
        startIndex = 0;
        
        diversityCache = -1;
    }
    
    @Override
    public void eat(LivingEntity entity, ItemStack stack) {
        if (!SOLOnion.CONFIG.isAllowed(entity.level(), stack) && !SOLOnion.CONFIG.shouldExcludedCount)
            return;
        
        startIndex--;
        if (startIndex < 0)
            startIndex = lastEaten.length - 1;
        
        lastEaten[startIndex] = stack.copy();
        updateDiversity(entity);
    }
    
    @Override
    public double simulateEat(LivingEntity entity, ItemStack stack) {
        if (!SOLOnion.CONFIG.isAllowed(entity.level(), stack) && !SOLOnion.CONFIG.shouldExcludedCount)
            return 0.0;
        
        List<ItemStack> stacks = new ArrayList<>(SOLOnion.CONFIG.trackCount);
        for (ItemStack toAdd : this)
            stacks.add(toAdd);
        
        if (stacks.size() == SOLOnion.CONFIG.trackCount)
            stacks.remove(stacks.size() - 1);
        stacks.add(0, stack);
        return calculateDiversity(stacks, entity) - foodDiversity(entity);
    }
    
    @Override
    public double foodDiversity(LivingEntity entity) {
        if (diversityCache == -1)
            updateDiversity(entity);
        return diversityCache;
    }
    
    @Override
    public int trackCount() {
        int count = 0;
        for (int i = 0; i < lastEaten.length; i++)
            if (lastEaten[i] != null)
                count++;
        return count;
    }
    
    @Override
    public int getLastEaten(LivingEntity entity, ItemStack food) {
        if (food.get(DataComponents.FOOD) == null)
            return -1;
        
        double d = SOLOnion.CONFIG.getDiversity(entity, food);
        int i = 0;
        for (ItemStack stack : this) {
            if (stack.getItem() == food.getItem() && SOLOnion.CONFIG.getDiversity(entity, stack) == d)
                return i;
            i++;
        }
        return -1;
    }
    
    @Override
    public boolean hasEaten(LivingEntity entity, ItemStack food) {
        if (food.get(DataComponents.FOOD) == null)
            return false;
        double d = SOLOnion.CONFIG.getDiversity(entity, food);
        for (ItemStack stack : this)
            if (stack.getItem() == food.getItem() && SOLOnion.CONFIG.getDiversity(entity, stack) == d)
                return true;
        return false;
    }
    
    @Override
    public void clearAll() {
        Arrays.fill(lastEaten, null);
        startIndex = lastEaten.length - 1;
        diversityCache = 0;
    }
    
    @Override
    public Iterator<ItemStack> iterator() {
        return new FilterIterator<>(new ArrayOffsetIterator<>(startIndex, lastEaten).iterator(), x -> x != null);
    }
    
    @Override
    public void configChanged() {
        if (lastEaten.length != SOLOnion.CONFIG.trackCount) {
            ItemStack[] newLastEaten = new ItemStack[SOLOnion.CONFIG.trackCount];
            int i = 0;
            for (ItemStack stack : this) {
                newLastEaten[i] = stack;
                i++;
                if (i >= newLastEaten.length)
                    break;
            }
            lastEaten = newLastEaten;
            startIndex = 0;
        }
        diversityCache = -1;
    }
}
