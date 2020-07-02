package de.siphalor.nbtcrafting.api.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;

import java.util.Collection;
import java.util.Map;

/**
 * An interface to use in exchange of {@link Recipe} which provides base functions used for remainder computation.
 *
 * @param <I>
 */
public interface NBTCRecipe<I extends Inventory> extends Recipe<I> {
	/**
	 * Should return all of the required ingredients in no particular order
	 *
	 * @return all ingredients
	 */
	default Collection<Ingredient> getIngredients() {
		return getPreviewInputs();
	}

	/**
	 * Builds the reference map used for dollar computation.
	 *
	 * @param inv the inventory for that this method is being called
	 * @return A map consisting of keys and belonging {@link net.minecraft.nbt.CompoundTag}s, {@link Number}s or {@link String}s
	 */
	Map<String, Object> buildDollarReference(I inv);
}
