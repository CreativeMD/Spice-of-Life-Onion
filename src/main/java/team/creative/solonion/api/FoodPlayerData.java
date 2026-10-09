package team.creative.solonion.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface FoodPlayerData extends Iterable<ItemStack> {

    public void serialize(ValueOutput output);

    public void deserialize(ValueInput input);

    public void eat(LivingEntity entity, ItemStack stack);

    public double simulateEat(LivingEntity entity, ItemStack stack);
    
    public double foodDiversity(LivingEntity entity);
    
    public void clearAll();
    
    public boolean hasEaten(LivingEntity entity, ItemStack food);
    
    public int getLastEaten(LivingEntity entity, ItemStack food);
    
    public void configChanged();
    
    public int trackCount();
    
}
