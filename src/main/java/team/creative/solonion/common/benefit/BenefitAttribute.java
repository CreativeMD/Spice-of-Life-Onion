package team.creative.solonion.common.benefit;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.creative.creativecore.common.config.gui.IGuiConfigParent;
import team.creative.creativecore.common.config.premade.registry.RegistryObjectConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.mod.FirstAidManager;

public class BenefitAttribute extends Benefit<Attribute> {
    
    public final Operation operation;
    
    public BenefitAttribute(Identifier location, double value, Operation op) {
        super(new RegistryObjectConfig<>(BuiltInRegistries.ATTRIBUTE, location), value);
        this.operation = op;
    }
    
    public BenefitAttribute(Holder<Attribute> holder, double value, Operation op) {
        this(holder.unwrapKey().get().identifier(), value);
    }
    
    public BenefitAttribute(Identifier identifier, double value) {
        this(identifier, value, Operation.ADD_VALUE);
    }
    
    public BenefitAttribute(Holder<Attribute> holder, double value) {
        this(holder, value, Operation.ADD_VALUE);
    }
    
    public BenefitAttribute(CompoundTag nbt) {
        super(BuiltInRegistries.ATTRIBUTE, nbt);
        operation = Operation.BY_ID.apply(nbt.getIntOr("op", 0));
    }
    
    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        nbt.putInt("op", operation.ordinal());
        return nbt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof BenefitAttribute att)
            return att.operation == operation;
        return false;
    }
    
    public static class BenefitTypeAttribute extends BenefitType<BenefitAttribute, Object2DoubleMap<AttributeHolder>, HashMap<AttributeHolder, AttributeModifier>> {
        
        public BenefitTypeAttribute(Function<CompoundTag, BenefitAttribute> factory) {
            super(factory);
        }
        
        @Override
        public Registry registry() {
            return BuiltInRegistries.ATTRIBUTE;
        }
        
        @Override
        public void createControls(GuiParent parent, IGuiConfigParent configParent) {
            parent.add(new GuiStateButton<Operation>(parent, "operation", new TextMapBuilder<Operation>().addComponent(Operation.values(), x -> Component.translatable(
                "config.solonion." + x.getSerializedName()))));
        }
        
        @Override
        public void loadValue(BenefitAttribute value, GuiParent parent, IGuiConfigParent configParent) {
            GuiStateButton<Operation> op = parent.get("operation");
            op.select(value.operation);
        }
        
        @Override
        public BenefitAttribute saveValue(Identifier identifier, double value, GuiParent parent, IGuiConfigParent configParent) {
            GuiStateButton<Operation> op = parent.get("operation");
            return new BenefitAttribute(identifier, value, op.selected());
        }
        
        @Override
        public Object2DoubleMap<AttributeHolder> createStack() {
            return new Object2DoubleArrayMap<>();
        }
        
        @Override
        public void addToStack(BenefitAttribute value, Object2DoubleMap<AttributeHolder> stack) {
            if (value.operation == Operation.ADD_VALUE)
                stack.compute(new AttributeHolder(value.property.getHolder(), value.operation), (x, y) -> y != null ? y + value.value : value.value);
            else
                stack.compute(new AttributeHolder(value.property.getHolder(), value.operation), (x, y) -> y != null ? Math.max(y, value.value) : value.value);
        }
        
        @Override
        public boolean isEmpty(Object2DoubleMap<AttributeHolder> stack) {
            return stack.isEmpty();
        }
        
        @Override
        public HashMap<AttributeHolder, AttributeModifier> createApplied() {
            return new HashMap<>();
        }
        
        @Override
        public void clearApplied(HashMap<AttributeHolder, AttributeModifier> applied) {
            applied.clear();
        }
        
        @Override
        public void saveApplied(HashMap<AttributeHolder, AttributeModifier> applied, ValueOutput output) {
            var list = output.childrenList(getId());
            for (Entry<AttributeHolder, AttributeModifier> entry : applied.entrySet()) {
                var child = list.addChild();
                child.putString("att", entry.getKey().attribute.getRegisteredName());
                child.putInt("op", entry.getKey().operation.ordinal());
                child.store("mod", AttributeModifier.CODEC, entry.getValue());
            }
        }
        
        @Override
        public void loadApplied(HashMap<AttributeHolder, AttributeModifier> applied, ValueInput input) {
            var list = input.childrenList(getId());
            if (list.isPresent())
                list.get().forEach(child -> {
                    Reference<Attribute> att = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(child.getStringOr("att", ""))).get();
                    if (att != null)
                        applied.put(new AttributeHolder(att, Operation.BY_ID.apply(child.getIntOr("op", 0))), child.read("mod", AttributeModifier.CODEC).orElseThrow());
                });
        }
        
        @Override
        public boolean apply(Player player, HashMap<AttributeHolder, AttributeModifier> applied, @Nullable Object2DoubleMap<AttributeHolder> stack) {
            float oldMax = player.getMaxHealth();
            
            if (!applied.isEmpty()) {
                for (Entry<AttributeHolder, AttributeModifier> entry : applied.entrySet())
                    player.getAttribute(entry.getKey().attribute).removeModifier(entry.getValue().id());
                applied.clear();
            }
            
            if (stack != null)
                for (var entry : stack.object2DoubleEntrySet()) {
                    var identifier = Identifier.tryBuild(SOLOnion.MODID, entry.getKey().operation.toString().toLowerCase());
                    var att = player.getAttribute(entry.getKey().attribute);
                    
                    att.removeModifier(identifier); // make sure modifier does not exist already
                    var modi = new AttributeModifier(identifier, entry.getDoubleValue(), entry.getKey().operation);
                    if (att != null) {
                        
                        att.addPermanentModifier(modi);
                        applied.put(entry.getKey(), modi);
                        if (entry.getKey().attribute == Attributes.MAX_HEALTH && !FirstAidManager.INSTALLED) {
                            // increase current health proportionally
                            float newHealth = player.getHealth() * player.getMaxHealth() / oldMax;
                            player.setHealth(newHealth);
                        }
                    }
                }
            return applied.isEmpty();
        }
    }
    
    private static record AttributeHolder(Holder<Attribute> attribute, Operation operation) {}
    
}
