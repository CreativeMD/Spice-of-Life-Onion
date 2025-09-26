package team.creative.solonion.common.network;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.SOLOnionAPI;

public class FoodListMessage extends CreativePacket {
    
    public CompoundTag data;
    
    public FoodListMessage() {}
    
    public FoodListMessage(Provider provider, FoodPlayerData foodList) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        foodList.serialize(output);
        this.data = output.buildResult();
    }
    
    @Override
    public void executeClient(Player player) {
        var input = TagValueInput.create(ProblemReporter.DISCARDING, player.registryAccess(), data);
        SOLOnionAPI.getFoodCapability(player).deserialize(input);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
