/**
 * Much of the following code was adapted from Cyclic's storage bag code.
 * Copyright for portions of the code are held by Samson Basset (Lothrazar)
 * as part of Cyclic, under the MIT license.
 */
package team.creative.solonion.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.capabilities.Capabilities;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.item.foodcontainer.FoodContainer;

public class FoodContainerScreen extends AbstractContainerScreen<FoodContainer> {
    public FoodContainerScreen(FoodContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        this.drawBackground(graphics, ResourceLocation.tryBuild(SOLOnion.MODID, "textures/gui/inventory.png"));
        var h = this.menu.containerItem.getCapability(Capabilities.ItemHandler.ITEM);
        if (h != null) {
            int slotsPerRow = h.getSlots();
            if (h.getSlots() > 9) {
                slotsPerRow = h.getSlots() / 2;
            }
            int xStart = (2 * 8 + 9 * 18 - slotsPerRow * 18) / 2;
            int yStart = 17 + 18;
            if (h.getSlots() > 9) {
                yStart = 17 + (84 - 36 - 23) / 2;
            }
            for (int i = 0; i < h.getSlots(); i++) {
                int row = i / slotsPerRow;
                int col = i % slotsPerRow;
                int xPos = xStart - 1 + col * 18;
                int yPos = yStart - 1 + row * 18;
                
                this.drawSlot(graphics, xPos, yPos);
            }
        }
    }
    
    protected void drawBackground(GuiGraphics graphics, ResourceLocation gui) {
        int relX = (this.width - this.getXSize()) / 2;
        int relY = (this.height - this.getYSize()) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, gui, relX, relY, 0, 0, this.getXSize(), this.getYSize(), 256, 256);
    }
    
    protected void drawSlot(GuiGraphics graphics, int x, int y, ResourceLocation texture, int size) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.getGuiLeft() + x, this.getGuiTop() + y, 0, 0, size, size, size, size);
    }
    
    protected void drawSlot(GuiGraphics graphics, int x, int y) {
        drawSlot(graphics, x, y, ResourceLocation.tryBuild(SOLOnion.MODID, "textures/gui/slot.png"), 18);
    }
}
