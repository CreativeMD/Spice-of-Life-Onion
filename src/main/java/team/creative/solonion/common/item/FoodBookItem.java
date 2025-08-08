package team.creative.solonion.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.solonion.client.gui.screen.FoodBookScreen;

public final class FoodBookItem extends BookItem {
    public FoodBookItem() {
        super(new Properties());
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (player.isLocalPlayer())
            openOnClient(player);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }
    
    @OnlyIn(Dist.CLIENT)
    private void openOnClient(Player player) {
        FoodBookScreen.open(player);
    }
}
