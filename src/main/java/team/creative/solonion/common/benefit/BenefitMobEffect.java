package team.creative.solonion.common.benefit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import team.creative.creativecore.common.config.gui.IGuiConfigParent;
import team.creative.creativecore.common.config.premade.registry.RegistryObjectConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.solonion.api.SOLOnionAPI;

public class BenefitMobEffect extends Benefit<MobEffect> {
    
    public BenefitMobEffect(ResourceLocation location, double value) {
        super(new RegistryObjectConfig<>(BuiltInRegistries.MOB_EFFECT, location), value);
    }
    
    public BenefitMobEffect(Holder<MobEffect> holder, double value) {
        this(holder.unwrapKey().get().location(), value);
    }
    
    public BenefitMobEffect(CompoundTag nbt) {
        super(BuiltInRegistries.MOB_EFFECT, nbt);
    }
    
    public static class BenefitTypeMobEffect extends BenefitType<BenefitMobEffect, Object2IntMap<Holder<MobEffect>>, AppliedMobEffects> {
        
        public BenefitTypeMobEffect(Function<CompoundTag, BenefitMobEffect> factory) {
            super(factory);
            NeoForge.EVENT_BUS.addListener(this::onEffectRemove);
        }
        
        public void onEffectRemove(MobEffectEvent.Remove event) {
            if (event.getEntity() instanceof Player player) {
                var applied = SOLOnionAPI.getBenefitCapability(player).getApplied(this);
                if (applied != null && !applied.reseting && applied.contains(event.getEffect()))
                    event.setCanceled(true);
            }
        }
        
        @Override
        public Registry registry() {
            return BuiltInRegistries.MOB_EFFECT;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        @Environment(EnvType.CLIENT)
        public void createControls(GuiParent parent, IGuiConfigParent configParent) {}
        
        @Override
        @OnlyIn(Dist.CLIENT)
        @Environment(EnvType.CLIENT)
        public void loadValue(BenefitMobEffect value, GuiParent parent, IGuiConfigParent configParent) {}
        
        @Override
        @OnlyIn(Dist.CLIENT)
        @Environment(EnvType.CLIENT)
        public BenefitMobEffect saveValue(ResourceLocation location, double value, GuiParent parent, IGuiConfigParent configParent) {
            return new BenefitMobEffect(location, value);
        }
        
        @Override
        public Object2IntMap<Holder<MobEffect>> createStack() {
            return new Object2IntArrayMap<>();
        }
        
        @Override
        public void addToStack(BenefitMobEffect value, Object2IntMap<Holder<MobEffect>> stack) {
            stack.compute(value.property.getHolder(), (x, y) -> y != null ? Math.max(y, (int) value.value) : (int) value.value);
        }
        
        @Override
        public boolean isEmpty(Object2IntMap<Holder<MobEffect>> stack) {
            return stack.isEmpty();
        }
        
        @Override
        public AppliedMobEffects createApplied() {
            return new AppliedMobEffects();
        }
        
        @Override
        public void clearApplied(AppliedMobEffects applied) {
            applied.clear();
        }
        
        @Override
        public Tag saveApplied(AppliedMobEffects applied) {
            ListTag list = new ListTag();
            for (Holder<MobEffect> effect : applied)
                list.add(StringTag.valueOf(effect.unwrapKey().get().location().toString()));
            return list;
        }
        
        @Override
        public void loadApplied(AppliedMobEffects applied, Tag nbt) {
            if (nbt instanceof ListTag list)
                for (int i = 0; i < list.size(); i++) {
                    Reference<MobEffect> mob = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(list.getStringOr(i, ""))).get();
                    if (mob != null)
                        applied.add(mob);
                }
        }
        
        @Override
        public boolean apply(Player player, AppliedMobEffects applied, @Nullable Object2IntMap<Holder<MobEffect>> stack) {
            applied.reseting = true;
            if (!applied.isEmpty()) {
                for (Holder<MobEffect> effect : applied)
                    player.removeEffect(effect);
                applied.clear();
            }
            applied.reseting = false;
            
            if (stack != null) {
                for (var entry : stack.object2IntEntrySet()) {
                    var in = new MobEffectInstance(entry.getKey(), -1, entry.getIntValue(), false, false);
                    if (player.addEffect(in))
                        applied.add(entry.getKey());
                }
            }
            return applied.isEmpty();
        }
    }
    
    private static class AppliedMobEffects implements Iterable<Holder<MobEffect>> {
        
        private List<Holder<MobEffect>> list = new ArrayList<>();
        boolean reseting;
        
        public void clear() {
            list.clear();
        }
        
        public void add(Holder<MobEffect> effect) {
            list.add(effect);
        }
        
        public boolean isEmpty() {
            return list.isEmpty();
        }
        
        public boolean contains(Holder<MobEffect> effect) {
            return list.contains(effect);
        }
        
        @Override
        public Iterator<Holder<MobEffect>> iterator() {
            return list.iterator();
        }
    }
    
}
