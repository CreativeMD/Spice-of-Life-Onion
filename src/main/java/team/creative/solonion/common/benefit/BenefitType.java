package team.creative.solonion.common.benefit;

import java.util.HashMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.creative.creativecore.common.config.gui.IGuiConfigParent;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.solonion.common.benefit.BenefitAttribute.BenefitTypeAttribute;
import team.creative.solonion.common.benefit.BenefitMobEffect.BenefitTypeMobEffect;

public abstract class BenefitType<T extends Benefit, L, A> {
    
    private static final NamedHandlerRegistry<BenefitType> REGISTRY = new NamedHandlerRegistry<>(null);
    private static final HashMap<Class, String> IDS = new HashMap<>();
    private static final HashMap<Class, BenefitType> TYPE_MAP = new HashMap<>();
    
    public static <R extends Benefit> void register(String id, Class<R> clazz, BenefitType<R, ?, ?> type) {
        REGISTRY.register(id, type);
        IDS.put(clazz, id);
        TYPE_MAP.put(clazz, type);
    }
    
    public static TextMapBuilder<BenefitType> typeMap() {
        return new TextMapBuilder<BenefitType>().addEntrySet(REGISTRY.entrySet(), x -> Component.translatable("config.solonion." + x.getKey()));
    }
    
    public static Benefit load(CompoundTag nbt) {
        BenefitType<?, ?, ?> type = REGISTRY.get(nbt.getStringOr("type", ""));
        if (type == null)
            throw new IllegalArgumentException("Could not find type " + nbt);
        return type.factory.apply(nbt);
    }
    
    public static BenefitType getType(Benefit benefit) {
        return TYPE_MAP.get(benefit.getClass());
    }
    
    public static BenefitType getType(Class<? extends Benefit> clazz) {
        return TYPE_MAP.get(clazz);
    }
    
    public static String getId(Benefit benefit) {
        return IDS.get(benefit.getClass());
    }
    
    public static String getId(Class<? extends Benefit> clazz) {
        return IDS.get(clazz);
    }
    
    public static Iterable<BenefitType> types() {
        return REGISTRY.values();
    }
    
    static {
        register("eff", BenefitMobEffect.class, new BenefitTypeMobEffect(BenefitMobEffect::new));
        register("att", BenefitAttribute.class, new BenefitTypeAttribute(BenefitAttribute::new));
    }
    
    public final Function<CompoundTag, T> factory;
    
    public BenefitType(Function<CompoundTag, T> factory) {
        this.factory = factory;
    }
    
    public String getId() {
        return REGISTRY.getId(this);
    }
    
    public abstract Registry registry();
    
    public abstract void createControls(GuiParent parent, IGuiConfigParent configParent);
    
    public abstract void loadValue(T value, GuiParent parent, IGuiConfigParent configParent);
    
    public abstract T saveValue(Identifier identifier, double value, GuiParent parent, IGuiConfigParent configParent);
    
    public abstract L createStack();
    
    public abstract void addToStack(T value, L stack);
    
    public abstract boolean isEmpty(L stack);
    
    public abstract A createApplied();
    
    /** @param player
     * @param applied
     * @param stack
     * @return whether applied is empty after the process and can be removed */
    public abstract boolean apply(Player player, A applied, @Nullable L stack);
    
    public abstract void clearApplied(A applied);
    
    public abstract void loadApplied(A applied, ValueInput input);
    
    public abstract void saveApplied(A applied, ValueOutput output);
    
}
