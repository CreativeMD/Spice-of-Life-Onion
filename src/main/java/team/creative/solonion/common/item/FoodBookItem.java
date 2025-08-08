package team.creative.solonion.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.KnowledgeBookItem;
import net.minecraft.world.level.Level;
import team.creative.solonion.client.gui.screen.FoodBookScreen;

public final class FoodBookItem extends KnowledgeBookItem {
    
    public FoodBookItem(Properties p) {
        super(p);
    }
    
    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (player.isLocalPlayer())
            openOnClient(player);
        return InteractionResult.SUCCESS;
    }
    
    private void openOnClient(Player player) {
        FoodBookScreen.open(player);
    }
}
