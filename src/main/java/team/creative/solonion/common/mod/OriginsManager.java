package team.creative.solonion.common.mod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.reflection.ReflectionHelper;
import team.creative.solonion.common.SOLOnion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OriginsManager {
    
    private static boolean loaded;
    private static Method isUsagePrevented = null;
    
    public static final boolean INSTALLED = CreativeCore.loader().isModLoaded("origins");
    
    public static boolean isEdible(Player player, ItemStack food) {
        if (!INSTALLED)
            return true;
        if (!loaded) {
            loaded = true;
            try {
                Class clazz = Class.forName("io.github.edwinmindcraft.apoli.common.power.PreventItemActionPower");
                isUsagePrevented = ReflectionHelper.findMethod(clazz, "isUsagePrevented", Entity.class, ItemStack.class);
            } catch (Exception e) {
                SOLOnion.LOGGER.error("Could not load Origins compatibility layer!", e);
            }
        }
        if (isUsagePrevented == null)
            return true;
        try {
            return !(boolean) isUsagePrevented.invoke(null, player, food);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            SOLOnion.LOGGER.error("Something went wrong with Origins compatibility layer!", e);
            return false;
        }
    }
    
}
