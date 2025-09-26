package team.creative.solonion.common;

import static net.minecraft.commands.Commands.literal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.client.SOLOnionClient;
import team.creative.solonion.common.command.FoodListCommand;
import team.creative.solonion.common.event.SOLOnionEvent;
import team.creative.solonion.common.item.SOLOnionItems;
import team.creative.solonion.common.item.foodcontainer.FoodContainerItem;
import team.creative.solonion.common.network.FoodListMessage;

@Mod(SOLOnion.MODID)
public final class SOLOnion {
    
    public static final String MODID = "solonion";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static CreativeNetwork NETWORK = new CreativeNetwork(1, LOGGER, ResourceLocation.tryBuild(SOLOnion.MODID, "main"));
    public static SOLOnionConfig CONFIG;
    public static SOLOnionEvent EVENT;
    
    public static boolean isActive(Player player) {
        return (!SOLOnion.CONFIG.limitProgressionToSurvival || player.gameMode().isSurvival());
    }
    
    public SOLOnion(IEventBus bus) {
        if (FMLLoader.getDist() == Dist.CLIENT)
            SOLOnionClient.load(bus);
        bus.addListener(this::setup);
        bus.addListener(this::registerCapabilities);
        SOLOnionItems.ITEMS.register(bus);
        SOLOnionItems.MENU_TYPES.register(bus);
        bus.addListener(SOLOnionItems::registerTabs);
        NeoForge.EVENT_BUS.addListener(this::command);
        SOLOnionAPI.ATTACHMENT_TYPES.register(bus);
    }
    
    public void setup(FMLCommonSetupEvent event) {
        NETWORK.registerType(FoodListMessage.class, FoodListMessage::new);
        
        NeoForge.EVENT_BUS.register(EVENT = new SOLOnionEvent());
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new SOLOnionConfig());
    }
    
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.ItemHandler.ITEM, (itemStack, context) -> {
            var handler = new ItemStackHandler(((FoodContainerItem) itemStack.getItem()).nslots);
            if (itemStack.has(DataComponents.CONTAINER)) {
                var container = itemStack.get(DataComponents.CONTAINER);
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (container.getSlots() <= i)
                        break;
                    handler.setStackInSlot(i, container.getStackInSlot(i));
                }
            }
            return handler;
        }, SOLOnionItems.LUNCHBOX.get(), SOLOnionItems.LUNCHBAG.get(), SOLOnionItems.GOLDEN_LUNCHBOX.get());
    }
    
    public void command(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal(FoodListCommand.name).then(FoodListCommand.withPlayerArgumentOrSender(literal("sync"), FoodListCommand::syncFoodList)).then(
            FoodListCommand.withPlayerArgumentOrSender(literal("clear"), FoodListCommand::clearFoodList)).then(FoodListCommand.withPlayerArgumentOrSender(literal("diversity"),
                FoodListCommand::displayDiversity)).then(FoodListCommand.withPlayerArgumentOrSender(literal("resetOrigin"), FoodListCommand::resetPlayerOrigin)).then(
                    FoodListCommand.withNoArgument(literal("resetAllOrigins"), FoodListCommand::resetAllOrigins)));
    }
    
}
