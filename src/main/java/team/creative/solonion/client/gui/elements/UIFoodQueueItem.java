package team.creative.solonion.client.gui.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import team.creative.solonion.client.SOLOnionClient;

/** Renders an ItemStack representing a food in the FoodList. Has a unique tooltip that displays that food item's
 * contribution to the food diversity. */
public class UIFoodQueueItem extends UIItemStack {
    private final int lastEaten;
    private final double diversity;
    
    public UIFoodQueueItem(ItemStack itemStack, double diversity, int lastEaten) {
        super(itemStack);
        this.lastEaten = lastEaten;
        this.diversity = diversity;
    }
    
    @Override
    protected void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<Component> tooltip = getFoodQueueTooltip();
        
        graphics.tooltip(mc.font, tooltip.stream().map(x -> ClientTooltipComponent.create(x.getVisualOrderText())).collect(Collectors.toList()), mouseX, mouseY,
            DefaultTooltipPositioner.INSTANCE, null);
    }
    
    private List<Component> getFoodQueueTooltip() {
        Component foodName = Component.translatable(itemStack.getItem().getDescriptionId()).withStyle(itemStack.getRarity().getStyleModifier());
        
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(foodName);
        
        Component space = Component.literal("");
        tooltip.add(space);
        
        SOLOnionClient.addTooltip(diversity, lastEaten, itemStack, tooltip, mc.player);
        
        return tooltip;
    }
}
