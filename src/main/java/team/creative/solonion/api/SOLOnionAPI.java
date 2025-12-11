package team.creative.solonion.api;

import java.util.function.Supplier;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.benefit.BenefitPlayerDataImpl;
import team.creative.solonion.common.food.FoodPlayerDataImpl;

public final class SOLOnionAPI {
    
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SOLOnion.MODID);
    
    public static final Identifier FOOD = Identifier.tryBuild(SOLOnion.MODID, "foodlist");
    public static final Identifier BENEFIT = Identifier.tryBuild(SOLOnion.MODID, "benefit");
    public static final Identifier FOODCONTAINER = Identifier.tryBuild(SOLOnion.MODID, "food_container");
    
    public static final Supplier<AttachmentType<FoodPlayerDataImpl>> FOOD_DATA = ATTACHMENT_TYPES.register(FOOD.getPath(), () -> AttachmentType.serializable(
        () -> new FoodPlayerDataImpl()).build());
    public static final Supplier<AttachmentType<BenefitPlayerDataImpl>> BENEFIT_DATA = ATTACHMENT_TYPES.register(BENEFIT.getPath(), () -> AttachmentType.serializable(
        () -> new BenefitPlayerDataImpl()).build());
    
    public static FoodPlayerData getFoodCapability(Player player) {
        if (player.hasData(FOOD_DATA))
            return player.getData(FOOD_DATA);
        FoodPlayerDataImpl food = new FoodPlayerDataImpl();
        player.setData(FOOD_DATA, food);
        return food;
    }
    
    public static BenefitPlayerData getBenefitCapability(Player player) {
        if (player.hasData(BENEFIT_DATA))
            return player.getData(BENEFIT_DATA);
        BenefitPlayerDataImpl benefit = new BenefitPlayerDataImpl();
        player.setData(BENEFIT_DATA, benefit);
        return benefit;
    }
    
    public static void syncFoodList(Player player) {
        SOLOnion.EVENT.syncFoodList(player);
    }
    
    private SOLOnionAPI() {}
    
}
