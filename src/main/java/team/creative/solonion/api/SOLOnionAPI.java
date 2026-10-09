package team.creative.solonion.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.ICreativeAttachmentType;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.benefit.BenefitPlayerDataImpl;
import team.creative.solonion.common.food.FoodPlayerDataImpl;

public final class SOLOnionAPI {
    public static final Identifier FOOD = Identifier.fromNamespaceAndPath(SOLOnion.MODID, "foodlist");
    public static final Identifier BENEFIT = Identifier.fromNamespaceAndPath(SOLOnion.MODID, "benefit");
    public static final Identifier FOODCONTAINER = Identifier.fromNamespaceAndPath(SOLOnion.MODID, "food_container");
    
    public static final ICreativeAttachmentType<FoodPlayerDataImpl> FOOD_DATA = CreativeCore.loader().registerAttachment(FOOD, FoodPlayerDataImpl::new, FoodPlayerDataImpl.MAP_CODEC);
    public static final ICreativeAttachmentType<BenefitPlayerDataImpl> BENEFIT_DATA = CreativeCore.loader().registerAttachment(BENEFIT, BenefitPlayerDataImpl::new, BenefitPlayerDataImpl.MAP_CODEC);
    
    public static FoodPlayerData getFoodCapability(Player player) {
        return FOOD_DATA.get(player);
    }
    
    public static BenefitPlayerData getBenefitCapability(Player player) {
        return BENEFIT_DATA.get(player);
    }
    
    public static void syncFoodList(Player player) {
        SOLOnion.EVENT.syncFoodList(player);
    }

    public static void init() {
        // Class load
    }

    private SOLOnionAPI() {}
    
}
