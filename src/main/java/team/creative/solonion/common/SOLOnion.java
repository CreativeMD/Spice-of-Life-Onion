package team.creative.solonion.common;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;

// TODO: ARIA
import net.neoforged.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.ICreativeLoader;
import team.creative.creativecore.Side;
import team.creative.creativecore.client.ClientLoader;
import team.creative.creativecore.common.CommonLoader;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.client.SOLOnionClient;
import team.creative.solonion.common.command.FoodListCommand;
import team.creative.solonion.common.event.SOLOnionEvent;
import team.creative.solonion.common.item.SOLOnionItems;
import team.creative.solonion.common.item.foodcontainer.FoodContainerItem;
import team.creative.solonion.common.network.FoodListMessage;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

@Mod(SOLOnion.MODID)
public final class SOLOnion implements CommonLoader, ClientLoader {
    public static final String MODID = "solonion";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static CreativeNetwork NETWORK = new CreativeNetwork(1, LOGGER, Identifier.tryBuild(SOLOnion.MODID, "main"));
    public static SOLOnionConfig CONFIG;
    public static SOLOnionEvent EVENT;

    public static boolean isActive(Player player) {
        return (!SOLOnion.CONFIG.limitProgressionToSurvival || player.gameMode().isSurvival());
    }

    public SOLOnion() {
        ICreativeLoader loader = CreativeCore.loader();
        SOLOnionAPI.init();
        SOLOnionItems.init();
        loader.register(this);
        loader.registerClient(this);
        if(loader.forge()) {
        loader.register(SOLOnionItems.COMMON_REGISTRY);
        }
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal(FoodListCommand.name).then(FoodListCommand.withPlayerArgumentOrSender(literal("sync"), FoodListCommand::syncFoodList)).then(
                FoodListCommand.withPlayerArgumentOrSender(literal("clear"), FoodListCommand::clearFoodList)).then(FoodListCommand.withPlayerArgumentOrSender(literal("diversity"),
                                                                                                                                                              FoodListCommand::displayDiversity)).then(FoodListCommand.withPlayerArgumentOrSender(literal("resetOrigin"), FoodListCommand::resetPlayerOrigin)).then(
                FoodListCommand.withNoArgument(literal("resetAllOrigins"), FoodListCommand::resetAllOrigins)));
    }

    @Override
    public void onInitialize() {
        var loader = CreativeCore.loader();
        if(loader.fabric()) {
            loader.register(SOLOnionItems.COMMON_REGISTRY);
        }
        loader.registerItemStorage((ItemStack itemStack) -> {
            List<ItemStack> list = new ArrayList<>(FoodContainerItem.getInventory(itemStack).itemCopies().toList());
            int size = ((FoodContainerItem) itemStack.getItem()).nslots;
            while (list.size() < size)
                list.add(ItemStack.EMPTY);
            return list;
        }, () -> new ItemLike[] { SOLOnionItems.LUNCHBOX.get(), SOLOnionItems.LUNCHBAG.get(), SOLOnionItems.GOLDEN_LUNCHBOX.get() });

        NETWORK.registerType(FoodListMessage.class, FoodListMessage::new);
        EVENT = new SOLOnionEvent();
        loader.registerPlayerJoin(EVENT::onPlayerLogin);
        loader.registerPlayerCopy(EVENT::onClone);
        loader.registerPlayerDimensionChange(EVENT::onPlayerDimensionChange);
        loader.registerPlayerRespawn(EVENT::onPlayerRespawn);
        loader.registerItemUsed(EVENT::onFoodEaten);
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new SOLOnionConfig());
        loader.registerModifyCreativeTab(SOLOnionItems::registerTabs, CreativeModeTabs.FOOD_AND_DRINKS);
    }

    @Override
    public void onInitializeClient() {
        var loader = CreativeCore.loader();
        if(loader.getOverallSide() == Side.CLIENT)
            SOLOnionClient.load(loader);

    }
}
