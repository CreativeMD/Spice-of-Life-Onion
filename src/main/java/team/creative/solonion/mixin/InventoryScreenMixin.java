package team.creative.solonion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import team.creative.solonion.client.gui.elements.UIInventoryButton;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    
    @WrapOperation(method = "init()V", require = 1, at = @At(value = "NEW", target = "net/minecraft/client/gui/components/ImageButton"))
    protected ImageButton createRecipeButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, Operation<ImageButton> operation) {
        
        return operation.call(x, y, width, height, sprites, (Button.OnPress) button -> {
            onPress.onPress(button);
            for (GuiEventListener listener : ((InventoryScreen) (Object) this).children())
                if (listener instanceof UIInventoryButton b) {
                    b.updateButtonPosition();
                    break;
                }
        });
    }
    
}
