/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.ingredient.IIngredient;

@Mixin(Recipe.class)
public interface MixinRecipe {
	@Shadow
	DefaultedList<Ingredient> getIngredients();

	/**
	 * @reason Returns the recipe remainders. Sadly has to overwrite since this is an interface.
	 * @author Siphalor
	 */
	@Overwrite
	default DefaultedList<ItemStack> getRemainder(Inventory inventory) {
		final DefaultedList<ItemStack> stackList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
		Map<String, Object> reference;
		List<Ingredient> ingredients;
		int[] resolvedIngredientStacks;
		if (this instanceof NBTCRecipe) {
			ingredients = new ArrayList<>(((NBTCRecipe<?>) this).getIngredients());
			// noinspection unchecked
			reference = ((NBTCRecipe<Inventory>) this).buildDollarReference(inventory);
			resolvedIngredientStacks = RecipeUtil.resolveIngredients(ingredients, inventory);
		} else {
			ingredients = getIngredients();
			resolvedIngredientStacks = RecipeUtil.resolveIngredients(ingredients, inventory);
			reference = RecipeUtil.buildReferenceMapFromResolvedIngredients(resolvedIngredientStacks, inventory);
		}

		for (int i = 0; i < stackList.size(); ++i) {
			ItemStack stack = inventory.getStack(i);
			int ingredientIndex = ArrayUtils.indexOf(resolvedIngredientStacks, i);
			if (ingredientIndex >= 0) {
				ItemStack remainder = ((IIngredient) (Object) ingredients.get(ingredientIndex)).nbtCrafting$getRecipeRemainder(stack, reference);
				if (remainder != null) {
					stackList.set(i, remainder);
					continue;
				}
			}
			if (stack.getItem().hasRecipeRemainder()) {
				stackList.set(i, new ItemStack(stack.getItem().getRecipeRemainder()));
			}
		}
		return stackList;
	}
}
