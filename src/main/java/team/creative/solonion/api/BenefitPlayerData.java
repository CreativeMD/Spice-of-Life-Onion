package team.creative.solonion.api;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import team.creative.solonion.common.benefit.BenefitStack;
import team.creative.solonion.common.benefit.BenefitType;

public interface BenefitPlayerData extends ValueIOSerializable {
    
    public void updateStack(Player player, BenefitStack benefits);
    
    public <T> T getApplied(BenefitType<?, ?, T> type);
    
}
