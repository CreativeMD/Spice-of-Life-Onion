package com.tarinoita.solsweetpotato.item.foodcontainer;

import javax.annotation.Nullable;

import com.tarinoita.solsweetpotato.lib.Localization;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class FoodContainerProvider implements MenuProvider {
    private String displayName;
    
    public FoodContainerProvider(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable(Localization.keyString("item", "container." + displayName));
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
        return new FoodContainer(i, playerInventory, player);
    }
}
