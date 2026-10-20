package team.creative.solonion.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ScreenAccessor {
	@Accessor
	public int getTopPos();
	@Accessor
	public int getLeftPos();
}
