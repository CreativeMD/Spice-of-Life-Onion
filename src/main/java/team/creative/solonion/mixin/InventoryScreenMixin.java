package team.creative.solonion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import team.creative.solonion.client.gui.elements.UIInventoryButton;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    
    @Inject(method = "onRecipeBookButtonClick()V", require = 1, at = @At("HEAD"))
    protected void onRecipeBookButtonClickStart(CallbackInfo info) {
        for (GuiEventListener listener : ((InventoryScreen) (Object) this).children())
            if (listener instanceof UIInventoryButton b) {
                b.updateButtonPosition();
                break;
            }
    }
    
}
