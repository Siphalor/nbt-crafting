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

package de.siphalor.nbtcrafting3.mixin;

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

import de.siphalor.nbtcrafting3.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting3.dollar.reference.MapBackedReferenceResolver;
import de.siphalor.nbtcrafting3.dollar.reference.ReferenceResolver;
import de.siphalor.nbtcrafting3.ingredient.IIngredient;

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
		ReferenceResolver referenceResolver;
		List<Ingredient> ingredients;
		int[] resolvedIngredientStacks;
		if (this instanceof NBTCRecipe) {
			ingredients = new ArrayList<>(((NBTCRecipe<?>) this).getIngredients());
			// noinspection unchecked
			referenceResolver = ((NBTCRecipe<Inventory>) this).getReferenceResolver(inventory);
			resolvedIngredientStacks = RecipeUtil.resolveIngredients(ingredients, inventory);
		} else {
			ingredients = getPreviewInputs();
			resolvedIngredientStacks = RecipeUtil.resolveIngredients(ingredients, inventory);
			referenceResolver = new MapBackedReferenceResolver(RecipeUtil.buildReferenceMapFromResolvedIngredients(resolvedIngredientStacks, inventory));
		}

		for (int i = 0; i < stackList.size(); ++i) {
			ItemStack stack = inventory.getStack(i);
			int ingredientIndex = ArrayUtils.indexOf(resolvedIngredientStacks, i);
			if (ingredientIndex >= 0) {
				ItemStack remainder = ((IIngredient) (Object) ingredients.get(ingredientIndex)).nbtCrafting3$getRecipeRemainder(stack, referenceResolver);
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
