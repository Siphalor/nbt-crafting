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

import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.ingredient.IIngredient;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(Recipe.class)
public interface MixinRecipe {
	@Shadow
	DefaultedList<Ingredient> getIngredients();

	@Inject(method = "getRemainder", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
	default void modifyRemainingStacks(Inventory inventory, CallbackInfoReturnable<DefaultedList<ItemStack>> cir, DefaultedList<ItemStack> stackList) {
		// The stackList is already pre-populated with Vanilla and Fabric API remainders
		List<Ingredient> ingredients;
		boolean customRecipe = this instanceof NBTCRecipe;
		if (customRecipe) {
			ingredients = new ArrayList<>(((NBTCRecipe<?>) this).getIngredients());
		} else {
			ingredients = getIngredients();
		}

		boolean shallContinue = false;
		for (Ingredient ingredient : ingredients) {
			if (((IIngredient) (Object) ingredient).nbtCrafting$isAdvanced()) {
				shallContinue = true;
				break;
			}
		}
		if (!shallContinue) {
			return;
		}

		// Resolve the ingredient indexes to the belonging stack indices
		int[] resolvedIngredientStacks = RecipeUtil.resolveIngredients(ingredients, inventory);
		Map<String, Object> reference;

		if (customRecipe) {
		// noinspection unchecked
			reference = ((NBTCRecipe<Inventory>) this).buildDollarReference(inventory, resolvedIngredientStacks);
		} else {
			reference = RecipeUtil.buildReferenceMapFromResolvedIngredients(resolvedIngredientStacks, inventory);
		}

		for (int i = 0; i < stackList.size(); ++i) {
			ItemStack stack = inventory.getStack(i);
			int ingredientIndex = ArrayUtils.indexOf(resolvedIngredientStacks, i);
			if (ingredientIndex >= 0) {
				IIngredient ingredient = (IIngredient) (Object) ingredients.get(ingredientIndex);
				// Simple, Vanilla-ish entries should already be set
				if (!ingredient.nbtCrafting$isAdvanced()) {
					continue;
				}

				ItemStack remainder = ingredient.nbtCrafting$getRecipeRemainder(stack, reference);
				if (remainder != null) {
					stackList.set(i, remainder);
				}
			}
		}
	}
}
