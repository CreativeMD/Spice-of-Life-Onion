package team.creative.solonion.common.benefit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.gui.IGuiConfigParent;
import team.creative.creativecore.common.config.key.ConfigKey;
import team.creative.creativecore.common.config.premade.registry.RegistryObjectConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextMapBuilder;

public abstract class Benefit<T> {
    
    static {
        ConfigTypeConveration.registerTypeCreator(Benefit.class, () -> new BenefitAttribute(Attributes.MAX_HEALTH, 2));
        
        ConfigTypeConveration.registerType(Benefit.class, new ConfigTypeConveration<Benefit>() {
            
            @Override
            public Benefit readElement(Provider provider, Benefit defaultValue, boolean loadDefault, boolean ignoreRestart, JsonElement element, Side side, ConfigKey key) {
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    if (object.has("attribute"))
                        return new BenefitAttribute(ResourceLocation.parse(object.get("attribute").getAsString()), object.get("value").getAsDouble());
                    return new BenefitMobEffect(ResourceLocation.parse(object.get("effect").getAsString()), object.get("value").getAsDouble());
                } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    try {
                        return BenefitType.load(TagParser.parseCompoundFully(element.getAsString()));
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                return defaultValue;
            }
            
            @Override
            public JsonElement writeElement(Provider provider, Benefit value, boolean saveDefault, boolean ignoreRestart, Side side, ConfigKey key) {
                return new JsonPrimitive(value.save().toString());
            }
            
            @Override
            public void createControls(GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
                parent.setFlow(GuiFlow.STACK_Y);
                parent.add(new GuiStateButton<BenefitType>(parent, "state", 0, BenefitType.typeMap()) {
                    
                    @Override
                    public void raiseEvent(GuiEvent event) {
                        GuiComboBox<ResourceLocation> box = (GuiComboBox<ResourceLocation>) parent.get("elements");
                        Registry registry = selected().registry();
                        GuiParent subConfig = parent.get("subConfig");
                        if (subConfig == null)
                            return;
                        subConfig.clear();
                        selected().createControls(subConfig, configParent);
                        box.set(new TextMapBuilder<ResourceLocation>().addComponent(registry.keySet(), value -> {
                            if (value.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                                return Component.literal(value.getPath());
                            return Component.literal(value.toString());
                        }));
                        super.raiseEvent(event);
                        reflow();
                    }
                    
                });
                parent.add(new GuiComboBox<ResourceLocation>(parent, "elements", new TextMapBuilder<ResourceLocation>()).setSearchbar(true));
                parent.add(new GuiTextfield(parent, "value").setFloatOnly().setDim(20, 6));
                parent.add(new GuiParent(parent, "subConfig"));
            }
            
            @Override
            public void loadValue(Benefit value, Benefit defaultValue, GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
                GuiStateButton<BenefitType> state = parent.get("state");
                state.select(BenefitType.getType(value));
                state.raiseEvent(new GuiControlChangedEvent(state));
                
                GuiComboBox<ResourceLocation> box = (GuiComboBox<ResourceLocation>) parent.get("elements");
                box.select(value.property.location);
                
                GuiTextfield text = parent.get("value");
                text.setText(value.value + "");
                
                GuiParent subConfig = parent.get("subConfig");
                state.selected().loadValue(value, subConfig, configParent);
            }
            
            @Override
            protected Benefit saveValue(GuiParent parent, IGuiConfigParent configParent, ConfigKey key, Side side) {
                GuiStateButton<BenefitType> state = parent.get("state");
                GuiComboBox<ResourceLocation> box = (GuiComboBox<ResourceLocation>) parent.get("elements");
                GuiTextfield text = parent.get("value");
                return state.selected().saveValue(box.selected(), text.parseDouble(), parent.get("subConfig"), configParent);
            }
            
            @Override
            public Benefit set(ConfigKey key, Benefit value) {
                return value;
            }
            
        });
    }
    
    public final RegistryObjectConfig<T> property;
    
    public double value;
    
    public Benefit(RegistryObjectConfig<T> property, double value) {
        this.property = property;
        this.value = value;
    }
    
    public Benefit(Registry<T> registry, CompoundTag nbt) {
        this.property = new RegistryObjectConfig<>(registry, ResourceLocation.tryParse(nbt.getStringOr("key", "")));
        this.value = nbt.getDoubleOr("val", 0);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Benefit benefit)
            return property.equals(benefit.property) && value == benefit.value;
        return false;
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", BenefitType.getId(this));
        nbt.putString("key", property.location.toString());
        nbt.putDouble("val", value);
        return nbt;
    }
    
}
