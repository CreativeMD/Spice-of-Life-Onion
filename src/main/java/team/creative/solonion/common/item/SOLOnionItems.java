package team.creative.solonion.common.item;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.item.foodcontainer.FoodContainer;
import team.creative.solonion.common.item.foodcontainer.FoodContainerItem;

public final class SOLOnionItems {
    
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SOLOnion.MODID);
    
    public static final Supplier<Item> BOOK = ITEMS.registerItem("food_book", FoodBookItem::new);
    public static final Supplier<Item> LUNCHBOX = ITEMS.registerItem("lunchbox", p -> new FoodContainerItem(p, 9, "lunchbox"));
    public static final Supplier<Item> LUNCHBAG = ITEMS.registerItem("lunchbag", p -> new FoodContainerItem(p, 5, "lunchbag"));
    public static final Supplier<Item> GOLDEN_LUNCHBOX = ITEMS.registerItem("golden_lunchbox", p -> new FoodContainerItem(p, 14, "golden_lunchbox"));
    
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, SOLOnion.MODID);
    public static final Supplier<MenuType<FoodContainer>> FOOD_CONTAINER = MENU_TYPES.register("food_container", () -> IMenuTypeExtension.create(((windowId, inv,
            data) -> new FoodContainer(windowId, inv, inv.player))));
    
    public static void registerTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(BOOK.get());
            event.accept(LUNCHBAG.get());
            event.accept(LUNCHBOX.get());
            event.accept(GOLDEN_LUNCHBOX.get());
        }
    }
}
