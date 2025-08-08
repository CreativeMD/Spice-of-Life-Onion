package team.creative.solonion.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import team.creative.solonion.common.item.SOLOnionItems;

public class UpgradeFoodContainer implements CraftingRecipe {
    
    final CraftingBookCategory category;
    final Ingredient input;
    final Ingredient material;
    final Holder<Item> result;
    
    public UpgradeFoodContainer(CraftingBookCategory cat, Ingredient input, Ingredient material, Holder<Item> result) {
        this.category = cat;
        this.input = input;
        this.material = material;
        this.result = result;
    }
    
    @Override
    public boolean matches(CraftingInput input, Level p_374075_) {
        if (input.ingredientCount() != 2) {
            return false;
        } else {
            boolean flag = false;
            boolean flag1 = false;
            
            for (int i = 0; i < input.size(); i++) {
                ItemStack itemstack = input.getItem(i);
                if (!itemstack.isEmpty()) {
                    if (!flag && this.input.test(itemstack)) {
                        flag = true;
                    } else {
                        if (flag1 || !this.material.test(itemstack)) {
                            return false;
                        }
                        
                        flag1 = true;
                    }
                }
            }
            
            return flag && flag1;
        }
    }
    
    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack itemstack = input.getItem(i);
            if (!itemstack.isEmpty() && this.input.test(itemstack)) {
                ItemStack upgraded = new ItemStack(result);
                upgraded.set(DataComponents.CONTAINER, itemstack.get(DataComponents.CONTAINER));
                return upgraded;
            }
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean isSpecial() {
        return true;
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width > 1 || height > 1;
    }
    
    @Override
    public ItemStack getResultItem(Provider provider) {
        return new ItemStack(result);
    }
    
    @Override
    public RecipeSerializer<UpgradeFoodContainer> getSerializer() {
        return SOLOnionItems.UPGRADE_FOOD_CONTAINER.get();
    }
    
    @Override
    public CraftingBookCategory category() {
        return this.category;
    }
    
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input);
        ingredients.add(material);
        return ingredients;
    }
    
    public static class Serializer implements RecipeSerializer<UpgradeFoodContainer> {
        private static final MapCodec<UpgradeFoodContainer> CODEC = RecordCodecBuilder.mapCodec(p_393288_ -> p_393288_.group(CraftingBookCategory.CODEC.fieldOf("category").orElse(
            CraftingBookCategory.MISC).forGetter(p_374281_ -> p_374281_.category), Ingredient.CODEC.fieldOf("input").forGetter(p_374483_ -> p_374483_.input), Ingredient.CODEC
                    .fieldOf("material").forGetter(p_374399_ -> p_374399_.material), ItemStack.ITEM_NON_AIR_CODEC.fieldOf("result").forGetter(p_393289_ -> p_393289_.result)).apply(
                        p_393288_, UpgradeFoodContainer::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeFoodContainer> STREAM_CODEC = StreamCodec.composite(CraftingBookCategory.STREAM_CODEC,
            p_374482_ -> p_374482_.category, Ingredient.CONTENTS_STREAM_CODEC, p_374160_ -> p_374160_.input, Ingredient.CONTENTS_STREAM_CODEC, p_374370_ -> p_374370_.material,
            ByteBufCodecs.holderRegistry(Registries.ITEM), p_393290_ -> p_393290_.result, UpgradeFoodContainer::new);
        
        @Override
        public MapCodec<UpgradeFoodContainer> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UpgradeFoodContainer> streamCodec() {
            return STREAM_CODEC;
        }
    }
    
}
