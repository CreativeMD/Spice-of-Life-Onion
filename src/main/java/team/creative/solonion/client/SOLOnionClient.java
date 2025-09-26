package team.creative.solonion.client;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.OnionFoodContainer;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.client.gui.elements.UIInventoryButton;
import team.creative.solonion.client.gui.screen.FoodBookScreen;
import team.creative.solonion.client.gui.screen.FoodContainerScreen;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.item.SOLOnionItems;

public class SOLOnionClient {
    
    public static KeyMapping OPEN_FOOD_BOOK;
    
    public static void load(IEventBus bus) {
        bus.addListener(SOLOnionClient::setupClient);
        bus.addListener(SOLOnionClient::registerMenu);
        bus.addListener(SOLOnionClient::registerKeybinds);
        NeoForge.EVENT_BUS.addListener(SOLOnionClient::handleKeypress);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, SOLOnionClient::onItemTooltip);
        NeoForge.EVENT_BUS.addListener(SOLOnionClient::addButton);
    }
    
    public static void setupClient(FMLClientSetupEvent event) {}
    
    public static void registerMenu(RegisterMenuScreensEvent event) {
        event.register(SOLOnionItems.FOOD_CONTAINER.get(), FoodContainerScreen::new);
    }
    
    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(OPEN_FOOD_BOOK = new KeyMapping("key.solonion.open_food_book", InputConstants.UNKNOWN.getValue(), "key.solonion.category"));
    }
    
    public static void handleKeypress(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;
        
        if (OPEN_FOOD_BOOK != null && OPEN_FOOD_BOOK.isDown())
            FoodBookScreen.open(player);
    }
    
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!SOLOnion.CONFIG.isFoodTooltipEnabled)
            return;
        
        Player player = event.getEntity();
        if (player == null)
            return;
        
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof OnionFoodContainer c)
            stack = c.getActualFood(player, stack);
        
        FoodProperties foodproperties = stack.getFoodProperties(player);
        if (foodproperties == null)
            return;
        
        FoodPlayerData food = SOLOnionAPI.getFoodCapability(player);
        addTooltip(food.simulateEat(player, stack), food.getLastEaten(player, stack), stack, event.getToolTip(), player);
    }
    
    public static void addButton(final ScreenEvent.Init.Post evt) {
        if (evt.getScreen() instanceof InventoryScreen s && SOLOnion.CONFIG.showButtonInInventory)
            evt.addListener(new UIInventoryButton(s));
    }
    
    public static void addTooltip(double diversity, int lastEaten, ItemStack stack, List<Component> tooltip, Player player) {
        boolean isAllowed = SOLOnion.CONFIG.isAllowed(stack);
        
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
