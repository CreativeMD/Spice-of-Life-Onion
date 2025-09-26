package team.creative.solonion.common.benefit;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.creative.solonion.api.BenefitPlayerData;

public class BenefitPlayerDataImpl implements BenefitPlayerData {
    
    private HashMap<BenefitType, Object> applied = new HashMap<>();
    
    @Override
    public void updateStack(Player player, BenefitStack benefits) {
        for (BenefitType type : BenefitType.types()) {
            var app = applied.get(type);
            var stack = benefits.get(type);
            if (app == null)
                if (stack == null)
                    continue;
                else
                    applied.put(type, app = type.createApplied());
            if (type.apply(player, app, stack))
                applied.remove(type);
        }
    }
    
    @Override
    public void serialize(ValueOutput output) {
        for (Entry<BenefitType, Object> entry : applied.entrySet()) {
            var child = output.child(entry.getKey().getId());
            entry.getKey().saveApplied(entry.getValue(), child);
            if (child.isEmpty())
                output.discard(entry.getKey().getId());
        }
    }
    
    @Override
    public void deserialize(ValueInput input) {
        for (Entry<BenefitType, Object> entry : applied.entrySet())
            entry.getKey().clearApplied(entry.getValue());
        
        if (input == null)
            return;
        
        for (BenefitType type : BenefitType.types()) {
            var child = input.child(type.getId());
            if (child.isPresent()) {
                var app = applied.get(type);
                if (app == null)
                    applied.put(type, app = type.createApplied());
                type.loadApplied(app, child.get());
            } else
                applied.remove(type);
        }
    }
    
    @Override
    public <T> T getApplied(BenefitType<?, ?, T> type) {
        return (T) applied.get(type);
    }
    
}
