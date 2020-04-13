package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.api.nbt.NbtHelper;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.ingredient.IIngredient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mixin(Recipe.class)
public interface MixinRecipe {
	@Shadow
	DefaultedList<Ingredient> getPreviewInputs();

	/**
	 * @reason Returns the recipe remainders. Sadly has to overwrite since this is an interface.
     * @author Siphalor
	 */
	@Overwrite
	default DefaultedList<ItemStack> getRemainingStacks(Inventory inventory) {
		final DefaultedList<ItemStack> stackList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
		Map<String, Object> reference;
        Collection<Ingredient> ingredients;
        if (this instanceof NBTCRecipe) {
	        ingredients = ((NBTCRecipe<?>) this).getIngredients();
	        // noinspection unchecked
	        reference = ((NBTCRecipe<Inventory>) this).buildDollarReference(inventory);
        } else {
        	DefaultedList<Ingredient> ingredientList = getPreviewInputs();
        	ingredients = ingredientList;
        	reference = new HashMap<>();
	        for (int j = 0; j < ingredientList.size(); j++) {
		        for (int i = 0; i < stackList.size(); i++) {
			        if (ingredientList.get(j).test(inventory.getStack(i)))
				        reference.putIfAbsent("i" + j, NbtHelper.getTagOrEmpty(inventory.getStack(i)));
		        }
	        }
        }
		main:
		for(int i = 0; i < stackList.size(); ++i) {
			ItemStack itemStack = inventory.getStack(i);
			for(Ingredient ingredient : ingredients) {
				if(ingredient.test(itemStack)) {
					//noinspection ConstantConditions
					ItemStack remainder = ((IIngredient)(Object) ingredient).getRecipeRemainder(itemStack, reference);
					if(remainder != null) {
						stackList.set(i, remainder);
						continue main;
					}
				}
			}
			if (itemStack.getItem().hasRecipeRemainder()) {
				stackList.set(i, new ItemStack(itemStack.getItem().getRecipeRemainder()));
			}
		}
		return stackList;
	}
}
