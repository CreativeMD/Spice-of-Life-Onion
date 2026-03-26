package team.creative.solonion.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import team.creative.solonion.common.SOLOnion;

public final class PageFlipButton extends Button {
    
    private static final Identifier texture = Identifier.tryBuild(SOLOnion.MODID, "textures/gui/food_book.png");
    public static final int width = 23;
    public static final int height = 13;
    
    private final Direction direction;
    private final Pageable pageable;
    
    public PageFlipButton(int x, int y, Direction direction, Pageable pageable) {
        super(x, y, 23, 13, Component.literal(""), (button) -> ((PageFlipButton) button).changePage(), Button.DEFAULT_NARRATION);
        
        this.direction = direction;
        this.pageable = pageable;
    }
    
    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;
        
        int textureX = 0;
        
        boolean isHovered = getX() <= mouseX && mouseX < getX() + width && getY() <= mouseY && mouseY < getY() + height;
        if (isHovered)
            textureX += 23;
        
        int textureY = direction == Direction.FORWARD ? 192 : 205;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), textureX, textureY, 23, 13, 256, 256);
    }
    
    public void updateState() {
        visible = pageable.isWithinRange(pageable.getCurrentPageNumber() + direction.distance);
    }
    
    private void changePage() {
        pageable.switchToPage(pageable.getCurrentPageNumber() + direction.distance);
    }
    
    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }
    
    public enum Direction {
        FORWARD(1),
        BACKWARD(-1);
        
        final int distance;
        
        Direction(int distance) {
            this.distance = distance;
        }
    }
    
    public interface Pageable {
        void switchToPage(int pageNumber);
        
        int getCurrentPageNumber();
        
        boolean isWithinRange(int pageNumber);
    }
}
