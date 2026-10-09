package team.creative.solonion.mixin;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;

@Mixin(ItemContainerContents.class)
public interface ItemContainerContentsAccessor {
	@Accessor
	public List<Optional<ItemStackTemplate>> getItems();

}
