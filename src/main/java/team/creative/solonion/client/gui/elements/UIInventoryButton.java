package team.creative.solonion.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.client.gui.screen.FoodBookScreen;
import team.creative.solonion.common.SOLOnion;

public class UIInventoryButton extends Button {
    
    private final AbstractContainerScreen screen;
    
    public UIInventoryButton(AbstractContainerScreen screen) {
        super(screen.getGuiLeft() + SOLOnion.CONFIG.buttonInventoryX, screen
                .getGuiTop() + SOLOnion.CONFIG.buttonInventoryY, SOLOnion.CONFIG.buttonInventoryWidth, SOLOnion.CONFIG.buttonInventoryHeight, Component.translatable(
                    "gui.solonion.inventory.button"), (button) -> Minecraft.getInstance().setScreen(new FoodBookScreen(Minecraft.getInstance().player)), DEFAULT_NARRATION);
        setTooltip(Tooltip.create(Component.translatable("gui.solonion.inventory.tooltip", TooltipUtils.print(SOLOnionAPI.getFoodCapability(Minecraft.getInstance().player)
                .foodDiversity(Minecraft.getInstance().player)))));
        this.screen = screen;
    }
    
    public void updateButtonPosition() {
        setX(screen.getGuiLeft() + SOLOnion.CONFIG.buttonInventoryX);
        setY(screen.getGuiTop() + SOLOnion.CONFIG.buttonInventoryY);
    }
}
