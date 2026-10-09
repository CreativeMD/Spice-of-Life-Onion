package team.creative.solonion.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.creative.solonion.common.benefit.BenefitStack;
import team.creative.solonion.common.benefit.BenefitType;

public interface BenefitPlayerData {

    public void serialize(ValueOutput output);

    public void deserialize(ValueInput input);

    public void updateStack(Player player, BenefitStack benefits);
    
    public <T> T getApplied(BenefitType<?, ?, T> type);
    
}
