package team.creative.solonion.common.item.foodcontainer;


import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FoodSlot extends Slot {
    
    public FoodSlot(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
    }
    
    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!canHold(stack))
            return false;
        return super.mayPlace(stack);
    }
    
    public static boolean canHold(ItemStack stack) {
        return stack.get(DataComponents.FOOD) != null;
    }
}
