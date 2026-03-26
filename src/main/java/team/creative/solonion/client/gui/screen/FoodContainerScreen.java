/**
 * Much of the following code was adapted from Cyclic's storage bag code.
 * Copyright for portions of the code are held by Samson Basset (Lothrazar)
 * as part of Cyclic, under the MIT license.
 */
package team.creative.solonion.client.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.item.foodcontainer.FoodContainer;

public class FoodContainerScreen extends AbstractContainerScreen<FoodContainer> {
    public FoodContainerScreen(FoodContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        this.extractBackground(graphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.extractTooltip(graphics, mouseX, mouseY);
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.drawBackground(graphics, Identifier.tryBuild(SOLOnion.MODID, "textures/gui/inventory.png"));
        var h = menu.containerItem.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(menu.containerItem));
        if (h != null) {
            int slotsPerRow = h.size();
            if (h.size() > 9) {
                slotsPerRow = h.size() / 2;
            }
            int xStart = (2 * 8 + 9 * 18 - slotsPerRow * 18) / 2;
            int yStart = 17 + 18;
            if (h.size() > 9) {
                yStart = 17 + (84 - 36 - 23) / 2;
            }
            for (int i = 0; i < h.size(); i++) {
                int row = i / slotsPerRow;
                int col = i % slotsPerRow;
                int xPos = xStart - 1 + col * 18;
                int yPos = yStart - 1 + row * 18;
                
                this.drawSlot(graphics, xPos, yPos);
            }
        }
    }
    
    protected void drawBackground(GuiGraphicsExtractor graphics, Identifier gui) {
        int relX = (this.width - this.getXSize()) / 2;
        int relY = (this.height - this.getYSize()) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, gui, relX, relY, 0, 0, this.getXSize(), this.getYSize(), 256, 256);
    }
    
    protected void drawSlot(GuiGraphicsExtractor graphics, int x, int y, Identifier texture, int size) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.getGuiLeft() + x, this.getGuiTop() + y, 0, 0, size, size, size, size);
    }
    
    protected void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        drawSlot(graphics, x, y, Identifier.tryBuild(SOLOnion.MODID, "textures/gui/slot.png"), 18);
    }
}
