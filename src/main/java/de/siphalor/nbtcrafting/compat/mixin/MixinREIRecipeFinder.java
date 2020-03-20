package de.siphalor.nbtcrafting.compat.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "me/shedaniel/rei/server/RecipeFinder")
public class MixinREIRecipeFinder {
	/**
	 * @author Siphalor
	 * @reason Fix copied code in REI
	 */
	@Overwrite
	public static int getItemId(ItemStack itemStack) {
		return net.minecraft.recipe.RecipeFinder.getItemId(itemStack);
	}

	/**
	 * @author Siphalor
	 * @reason Fix copied code in REI
	 */
	@Overwrite
	public static ItemStack getStackFromId(int itemId) {
		return net.minecraft.recipe.RecipeFinder.getStackFromId(itemId);
	}
}
