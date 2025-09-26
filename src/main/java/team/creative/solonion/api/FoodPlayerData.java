package team.creative.solonion.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public interface FoodPlayerData extends ValueIOSerializable, Iterable<ItemStack> {
    
    public void eat(LivingEntity entity, ItemStack stack);
    
    public double simulateEat(LivingEntity entity, ItemStack stack);
    
    public double foodDiversity(LivingEntity entity);
    
    public void clearAll();
    
    public boolean hasEaten(LivingEntity entity, ItemStack food);
    
    public int getLastEaten(LivingEntity entity, ItemStack food);
    
    public void configChanged();
    
    public int trackCount();
    
}
