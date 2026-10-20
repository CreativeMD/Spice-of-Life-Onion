package team.creative.solonion.mixin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

@Mixin(ItemContainerContents.class)
public interface ItemContainerContentsAccessor {
    @Accessor
    public List<Optional<ItemStackTemplate>> getItems();
    
}
