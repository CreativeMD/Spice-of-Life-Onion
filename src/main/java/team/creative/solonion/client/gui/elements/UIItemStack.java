package team.creative.solonion.client.gui.elements;

import static net.minecraft.world.item.TooltipFlag.ADVANCED;
import static net.minecraft.world.item.TooltipFlag.NORMAL;

import java.awt.Rectangle;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;

public class UIItemStack extends UIElement {
    public static final int size = 16;
    
    public ItemStack itemStack;
    
    public UIItemStack(ItemStack itemStack) {
        super(new Rectangle(size, size));
        
        this.itemStack = itemStack;
    }
    
    @Override
    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        
        graphics.renderItem(itemStack, frame.x + (frame.width - size) / 2, frame.y + (frame.height - size) / 2);
    }
    
    @Override
    protected boolean hasTooltip() {
        return true;
    }
    
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Component> tooltip = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, mc.options.advancedItemTooltips ? ADVANCED : NORMAL);
        graphics.renderTooltip(mc.font, tooltip.stream().map(x -> ClientTooltipComponent.create(x.getVisualOrderText())).collect(Collectors.toList()), mouseX, mouseY,
            DefaultTooltipPositioner.INSTANCE, null);
    }
}
