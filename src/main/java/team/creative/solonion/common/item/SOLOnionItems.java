package team.creative.solonion.common.item;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.item.foodcontainer.FoodContainer;
import team.creative.solonion.common.item.foodcontainer.FoodContainerItem;
import team.creative.solonion.common.recipe.UpgradeFoodContainer;

public final class SOLOnionItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SOLOnion.MODID);
    
    public static final Supplier<Item> BOOK = ITEMS.register("food_book", () -> new FoodBookItem());
    public static final Supplier<Item> LUNCHBOX = ITEMS.register("lunchbox", () -> new FoodContainerItem(9, "lunchbox"));
    public static final Supplier<Item> LUNCHBAG = ITEMS.register("lunchbag", () -> new FoodContainerItem(5, "lunchbag"));
    public static final Supplier<Item> GOLDEN_LUNCHBOX = ITEMS.register("golden_lunchbox", () -> new FoodContainerItem(14, "golden_lunchbox"));
    
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, SOLOnion.MODID);
    public static final Supplier<MenuType<FoodContainer>> FOOD_CONTAINER = MENU_TYPES.register("food_container", () -> IMenuTypeExtension.create(((windowId, inv,
            data) -> new FoodContainer(windowId, inv, inv.player))));
    
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, SOLOnion.MODID);
    public static final Supplier<RecipeSerializer<UpgradeFoodContainer>> UPGRADE_FOOD_CONTAINER = RECIPE_SERIALIZERS.register("upgrade_food_container",
        () -> new UpgradeFoodContainer.Serializer());
    
    public static void registerTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(BOOK.get());
            event.accept(LUNCHBAG.get());
            event.accept(LUNCHBOX.get());
            event.accept(GOLDEN_LUNCHBOX.get());
        }
    }
}
