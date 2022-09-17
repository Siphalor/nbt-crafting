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

package de.siphalor.nbtcrafting.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.ingredient.IIngredient;

public class RecipeUtil {
	@Deprecated
	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, DefaultedList<Ingredient> ingredients, Inventory inventory) {
		return getDollarAppliedResult(baseOutput, ingredients, inventory);
	}

	public static ItemStack getDollarAppliedResult(ItemStack baseOutput, DefaultedList<Ingredient> ingredients, Inventory inventory) {
		ItemStack stack = baseOutput.copy();
		Dollar[] dollars = DollarParser.extractDollars(stack.getTag(), true);

		if (dollars.length > 0) {
			return applyDollars(stack, dollars, buildReferenceMapFromResolvedIngredients(resolveIngredients(ingredients, inventory), inventory));
		}
		return stack;
	}

	public static Map<String, Object> buildReferenceMapFromResolvedIngredients(int[] resolvedIngredientStacks, Inventory inventory) {
		Map<String, Object> reference = new HashMap<>();
		for (int i = 0; i < resolvedIngredientStacks.length; i++) {
			int resolvedIngredientStack = resolvedIngredientStacks[i];
			if (resolvedIngredientStack != -1) {
				reference.put("i" + i, inventory.getStack(resolvedIngredientStack));
			}
		}
		return reference;
	}

	public static int[] resolveIngredients(List<Ingredient> ingredients, Inventory inventory) {
		final int ingredientCount = ingredients.size();
		final int inventorySize = inventory.size();
		int[] resolvedIngredientStacks = new int[ingredientCount];
		boolean[] stackMatchesToAnything = new boolean[inventorySize]; // whether a stack has been resolved already
		byte[] matches = new byte[ingredientCount * inventorySize]; // 0 = unchecked, 1 = match, -1 = no match

		boolean advancedMatchingRequired = false;

		// try greedy matching
		outer:
		for (int j = 0; j < ingredientCount; j++) {
			Ingredient ingredient = ingredients.get(j);
			int ingredientMatchesOffset = j * inventorySize;
			for (int i = 0; i < inventorySize; i++) {
				if (stackMatchesToAnything[i])
					continue;

				if (ingredient.test(inventory.getStack(i))) {
					resolvedIngredientStacks[j] = i;
					matches[ingredientMatchesOffset + i] = 1;
					stackMatchesToAnything[i] = true;
					continue outer;
				} else {
					matches[ingredientMatchesOffset + i] = -1;
				}
			}

			// ingredient could not be matched
			advancedMatchingRequired = true;
			break;
		}

		if (!advancedMatchingRequired) {
			return resolvedIngredientStacks;
		}

		// fill rest of the match matrix
		for (int j = 0; j < ingredientCount; j++) {
			Ingredient ingredient = ingredients.get(j);
			int ingredientMatchesOffset = j * inventorySize;
			for (int i = 0; i < inventorySize; i++) {
				if (matches[ingredientMatchesOffset + i] == 0) { // combination has not been checked yet
					if (ingredient.test(inventory.getStack(i))) {
						matches[ingredientMatchesOffset + i] = 1;
					} else {
						matches[ingredientMatchesOffset + i] = -1;
					}
				}
			}
		}

		// try a reverse brute force matching
		int currentIngredient = 0; // the ingredient currently being matched
		int[] ingredientStackIndices = new int[ingredientCount]; // the stack indices currently being matched for each ingredient
		boolean[] usedStacks = new boolean[inventorySize]; // whether stacks are used in the current matching
		ingredientStackIndices[0] = inventorySize; // for reverse matching

		outer:
		while (true) {
			final int ingredientMatchesOffset = currentIngredient * inventorySize;
			int ingredientStackIndex = ingredientStackIndices[currentIngredient]; // temp variable to avoid constant array access
			while (true) {
				ingredientStackIndex--;

				if (ingredientStackIndex < 0) { // no matching stacks found
					currentIngredient--; // continue matching with the previous ingredient
					if (currentIngredient < 0) { // if there is no previous ingredient, no matching is possible
						// this should technically never happen, as recipes are checked for validity before reference maps are built
						NbtCrafting.logError("Failed to build reference map dynamically for recipe! Please report this on the Nbt Crafting issue tracker!");
						break outer;
					}

					// mark stack as free again
					usedStacks[ingredientStackIndices[currentIngredient]] = false;
					continue outer;
				}
				if (usedStacks[ingredientStackIndex]) { // stack is already in use
					continue;
				}

				if (matches[ingredientMatchesOffset + ingredientStackIndex] == 1) { // stack matches to the ingredient
					// mark stack as matched
					ingredientStackIndices[currentIngredient] = ingredientStackIndex;
					usedStacks[ingredientStackIndex] = true;

					currentIngredient++; // continue with the next ingredient
					if (currentIngredient >= ingredientCount) { // all ingredients have been matched, we're done
						break outer;
					}
					ingredientStackIndices[currentIngredient] = inventorySize; // for reverse matching

					continue outer;
				}
			}
		}

		return ingredientStackIndices;
	}

	@Deprecated
	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, Ingredient ingredient, Inventory inventory) {
		return getDollarAppliedResult(baseOutput, ingredient, inventory);
	}

	public static ItemStack getDollarAppliedResult(ItemStack baseOutput, Ingredient ingredient, Inventory inventory) {
		return getDollarAppliedResult(baseOutput, ingredient, "this", inventory);
	}

	@Deprecated
	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, Ingredient ingredient, String referenceName, Inventory inventory) {
		return getDollarAppliedResult(baseOutput, ingredient, referenceName, inventory);
	}

	public static ItemStack getDollarAppliedResult(ItemStack baseOutput, Ingredient ingredient, String referenceName, Inventory inventory) {
		ItemStack stack = baseOutput.copy();
		Dollar[] dollars = DollarParser.extractDollars(stack.getTag(), true);

		if (dollars.length > 0) {
			Map<String, Object> reference = new HashMap<>();
			reference.put(referenceName, NbtUtil.getTagOrEmpty(inventory.getStack(0)));

			return applyDollars(stack, dollars, reference);
		}
		return stack;
	}

	public static ItemStack getRemainder(ItemStack itemStack, Ingredient ingredient, Map<String, Object> reference) {
		ItemStack result = ((IIngredient) (Object) ingredient).nbtCrafting$getRecipeRemainder(itemStack, reference);
		if (result == null) {
			return new ItemStack(itemStack.getItem().getRecipeRemainder());
		}
		return result;
	}

	public static void putRemainders(DefaultedList<ItemStack> remainders, Inventory target, World world, BlockPos scatterPos) {
		putRemainders(remainders, target, world, scatterPos, 0);
	}

	public static void putRemainders(DefaultedList<ItemStack> remainders, Inventory target, World world, BlockPos scatterPos, int offset) {
		final int size = remainders.size();
		if (size > target.size()) {
			throw new IllegalArgumentException("Size of given remainder list must be <= size of target inventory");
		}
		for (int i = 0; i < size; i++) {
			if (target.getStack(offset + i).isEmpty()) {
				target.setStack(offset + i, remainders.get(i));
				remainders.set(i, ItemStack.EMPTY);
			}
		}
		ItemScatterer.spawn(world, scatterPos, remainders);
	}

	public static ItemStack applyDollars(ItemStack stack, Dollar[] dollars, Map<String, Object> reference) {
		for (Dollar dollar : dollars) {
			try {
				dollar.apply(stack, reference);
			} catch (DollarException e) {
				e.printStackTrace();
			}
		}
		if (stack.getDamage() > stack.getMaxDamage()) {
			return ItemStack.EMPTY;
		}
		return stack;
	}
}
