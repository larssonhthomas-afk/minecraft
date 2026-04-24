package com.dropnr.item;

import com.dropnr.logic.BossKeyType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * Bygger firework_star-itemstacks för bossnycklar. Färgen sätts via
 * FireworkExplosionComponent och displaynamn via CUSTOM_NAME.
 */
public final class KeyItemFactory {

    private KeyItemFactory() {}

    public static ItemStack create(BossKeyType keyType) {
        ItemStack stack = new ItemStack(Items.FIREWORK_STAR);

        IntList colors = new IntArrayList();
        colors.add(keyType.colorRgb());
        IntList fadeColors = new IntArrayList();

        FireworkExplosionComponent explosion = new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.SMALL_BALL,
                colors,
                fadeColors,
                false,
                false);

        stack.set(DataComponentTypes.FIREWORK_EXPLOSION, explosion);
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(keyType.displayName()));
        return stack;
    }
}
