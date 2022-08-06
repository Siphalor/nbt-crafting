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
import java.util.Map;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;
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
			Map<String, Object> references = new HashMap<>();
			ingredient:
			for (int j = 0; j < ingredients.size(); j++) {
				for (int i = 0; i < inventory.getInvSize(); i++) {
					if (ingredients.get(j).test(inventory.getInvStack(i))) {
						references.putIfAbsent("i" + j, inventory.getInvStack(i));
						continue ingredient;
					}
				}
			}

			return applyDollars(stack, dollars, new DollarRuntime(references::get));
		}
		return stack;
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
			return applyDollars(stack, dollars, new DollarRuntime(key -> {
				if (referenceName.equals(key)) {
					return inventory.getInvStack(0);
				}
				return null;
			}));
		}
		return stack;
	}

	public static ItemStack getRemainder(ItemStack itemStack, Ingredient ingredient, Map<String, Object> references) {
		return getRemainder(itemStack, ingredient, new DollarRuntime(references::get));
	}
	public static ItemStack getRemainder(ItemStack itemStack, Ingredient ingredient, DollarRuntime runtime) {
		ItemStack result = ((IIngredient) (Object) ingredient).nbtCrafting$getRecipeRemainder(itemStack, runtime);
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
		if (size > target.getInvSize()) {
			throw new IllegalArgumentException("Size of given remainder list must be <= size of target inventory");
		}
		for (int i = 0; i < size; i++) {
			if (target.getInvStack(offset + i).isEmpty()) {
				target.setInvStack(offset + i, remainders.get(i));
				remainders.set(i, ItemStack.EMPTY);
			}
		}
		ItemScatterer.spawn(world, scatterPos, remainders);
	}

	@Deprecated
	public static ItemStack applyDollars(ItemStack stack, Dollar[] dollars, Map<String, Object> references) {
		return applyDollars(stack, dollars, new DollarRuntime(references::get));
	}
	public static ItemStack applyDollars(ItemStack stack, Dollar[] dollars, DollarRuntime runtime) {
		for (Dollar dollar : dollars) {
			try {
				dollar.apply(stack, runtime);
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
