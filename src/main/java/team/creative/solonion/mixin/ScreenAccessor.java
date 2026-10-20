package team.creative.solonion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public interface ScreenAccessor {
    @Accessor
    public int getTopPos();
    
    @Accessor
    public int getLeftPos();
}
