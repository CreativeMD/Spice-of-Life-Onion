package team.creative.solonion.common.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import team.creative.creativecore.CommonRegistry;
import team.creative.creativecore.CommonRegistry.BoundRegistry;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.item.foodcontainer.FoodContainer;
import team.creative.solonion.common.item.foodcontainer.FoodContainerItem;

import java.util.function.Supplier;

public final class SOLOnionItems {
    public static final CommonRegistry COMMON_REGISTRY = new CommonRegistry();
    public static final CommonRegistry.NamespacedRegistry REGISTRY = COMMON_REGISTRY.bindToNamespace(SOLOnion.MODID);
    public static final BoundRegistry.Items ITEMS = REGISTRY.createItems();

    public static final Supplier<Item> BOOK = ITEMS.registerItem("food_book", FoodBookItem::new);
    public static final Supplier<Item> LUNCHBOX = ITEMS.registerItem("lunchbox", p -> new FoodContainerItem(p, 9, "lunchbox"));
    public static final Supplier<Item> LUNCHBAG = ITEMS.registerItem("lunchbag", p -> new FoodContainerItem(p, 5, "lunchbag"));
    public static final Supplier<Item> GOLDEN_LUNCHBOX = ITEMS.registerItem("golden_lunchbox", p -> new FoodContainerItem(p, 14, "golden_lunchbox"));

    public static final BoundRegistry<MenuType<?>> MENU_TYPES = REGISTRY.bindToRegistry(BuiltInRegistries.MENU);
    public static final MenuType<FoodContainer> FOOD_CONTAINER = new MenuType<>((containerId, inventory) -> new FoodContainer(containerId, inventory, inventory.player), FeatureFlags.DEFAULT_FLAGS);

    static {
        MENU_TYPES.register("food_container", () -> FOOD_CONTAINER);
    }

    public static void registerTabs(CreativeModeTab.Output output) {
        output.accept(BOOK.get());
        output.accept(LUNCHBAG.get());
        output.accept(LUNCHBOX.get());
        output.accept(GOLDEN_LUNCHBOX.get());
    }
    public static void init() {
        // Used to force classloading.
    };
}
