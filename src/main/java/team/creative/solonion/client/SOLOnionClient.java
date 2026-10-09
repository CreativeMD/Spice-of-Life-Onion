package team.creative.solonion.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import team.creative.creativecore.ICreativeLoader;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.OnionFoodContainer;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.client.gui.elements.UIInventoryButton;
import team.creative.solonion.client.gui.screen.FoodBookScreen;
import team.creative.solonion.client.gui.screen.FoodContainerScreen;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.item.SOLOnionItems;

import java.util.List;

public class SOLOnionClient {
    
    public static final KeyMapping.Category SOL_CATEGORY = CreativeCoreClient.loader().registerCategory(Identifier.fromNamespaceAndPath(SOLOnion.MODID, "category"));
    public static final KeyMapping OPEN_FOOD_BOOK = new KeyMapping("key.solonion.open_food_book", InputConstants.UNKNOWN.getValue(), SOL_CATEGORY);
    
    public static void load(ICreativeLoader loader) {
        var clientLoader = CreativeCoreClient.loader();
        loader.registerClientTick(SOLOnionClient::handleKeypress);
        clientLoader.registerMenu(SOLOnionItems.FOOD_CONTAINER, FoodContainerScreen::new);
        clientLoader.registerKeybind(OPEN_FOOD_BOOK);
        clientLoader.registerModifyTooltip(SOLOnionClient::onItemTooltip);
        clientLoader.addScreenWidget(screen -> screen instanceof InventoryScreen s && SOLOnion.CONFIG.showButtonInInventory ? s : null, UIInventoryButton::new);
    }
    
    public static void handleKeypress() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;
        
        if (OPEN_FOOD_BOOK != null && OPEN_FOOD_BOOK.isDown())
            FoodBookScreen.open(player);
    }
    
    public static void onItemTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> lines) {
        if (!SOLOnion.CONFIG.isFoodTooltipEnabled)
            return;
        
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        if (stack.getItem() instanceof OnionFoodContainer c)
            stack = c.getActualFood(player, stack);
        
        FoodProperties foodproperties = stack.get(DataComponents.FOOD);
        if (foodproperties == null)
            return;
        
        FoodPlayerData food = SOLOnionAPI.getFoodCapability(player);
        addTooltip(food.simulateEat(player, stack), food.getLastEaten(player, stack), stack, lines, player);
    }
    
    public static void addTooltip(double diversity, int lastEaten, ItemStack stack, List<Component> tooltip, Player player) {
        boolean isAllowed = SOLOnion.CONFIG.isAllowed(player.level(), stack);
        
        if (!isAllowed) {
            if (SOLOnion.CONFIG.showDisabledTooltip)
                tooltip.add(Component.translatable("gui.solonion.tooltip.disabled").withStyle(style -> style.applyFormat(ChatFormatting.DARK_GRAY)));
            return;
        }
        
        ChatFormatting color = ChatFormatting.GRAY;
        if (diversity < 0)
            color = ChatFormatting.RED;
        else if (diversity > 0)
            color = ChatFormatting.GREEN;
        var text = Component.translatable("gui.solonion.tooltip.diversity").append(": " + String.format("%.2f", SOLOnion.CONFIG.getDiversity(player, stack))).withStyle(
            ChatFormatting.GRAY);
        if (SOLOnion.CONFIG.showDiversityChangeInTooltip)
            text = text.append(" (").append(Component.literal(String.format("%.2f", diversity)).withStyle(color)).append(")");
        tooltip.add(text);
        if (lastEaten != -1) {
            String last_eaten_path = "tooltip.last_eaten";
            if (lastEaten == 1)
                last_eaten_path = "tooltip.last_eaten_singular";
            tooltip.add(Component.translatable("gui.solonion." + last_eaten_path, lastEaten).withStyle(ChatFormatting.GRAY));
        }
    }
}
