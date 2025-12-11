package team.creative.solonion.client.gui.elements;

import java.awt.Rectangle;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class UIImage extends UIElement {
    public Image image;
    public float alpha = 1;
    
    public UIImage(Image image) {
        this(new Rectangle(image.partOfTexture.getSize()), image);
    }
    
    public UIImage(Rectangle frame, Image image) {
        super(frame);
        
        this.image = image;
    }
    
    @Override
    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        
        int imageWidth = image.partOfTexture.width;
        int imageHeight = image.partOfTexture.height;
        
        graphics.blit(RenderPipelines.GUI_TEXTURED, image.identifier, frame.x + (int) Math.floor((frame.width - imageWidth) / 2d), frame.y + (int) Math.floor(
            (frame.height - imageHeight) / 2d), image.partOfTexture.x, image.partOfTexture.y, imageWidth, imageHeight, 256, 256);
    }
    
    public static class Image {
        public final Identifier identifier;
        public final Rectangle partOfTexture;
        
        public Image(Identifier identifier, Rectangle partOfTexture) {
            this.identifier = identifier;
            this.partOfTexture = partOfTexture;
        }
    }
}
