package team.creative.solonion.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.creativecore.CreativeCore;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {
	@Inject(method = "eat", at = @At("RETURN"))
	private static void wrapEatResult(LevelAccessor level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<InteractionResult> cir) {
		if(cir.getReturnValue().consumesAction()) {
			Item eatenItem = Items.CAKE;
			var loader = CreativeCore.loader();
			// If Farmer's Delight is installed, replace "cake" with FD's "cake slice"
			if (loader.isModLoaded("farmersdelight"))
				eatenItem = BuiltInRegistries.ITEM.getValue(Identifier.tryBuild("farmersdelight", "cake_slice"));
			ItemStack eatenItemStack = new ItemStack(eatenItem);
			// Fire an event instead of directly updating the food list, so that
			// SoL: Carrot Edition registers the eaten food too.
			loader.publishItemUsed(player, eatenItemStack);
		}
	}
}
