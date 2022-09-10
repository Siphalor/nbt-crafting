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

package de.siphalor.nbtcrafting3.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting3.NbtCrafting;
import de.siphalor.nbtcrafting3.api.RecipeUtil;
import de.siphalor.nbtcrafting3.dollar.exception.UnresolvedDollarReferenceException;

public class BrewingRecipe extends IngredientRecipe<Inventory> {
	public static final RecipeSerializer<BrewingRecipe> SERIALIZER = new IngredientRecipe.Serializer<>(BrewingRecipe::new);

	public BrewingRecipe(Identifier identifier, Ingredient base, Ingredient ingredient, ItemStack result, Serializer<BrewingRecipe> serializer) {
		super(identifier, base, ingredient, result, NbtCrafting.BREWING_RECIPE_TYPE, serializer);
	}

	@Override
	public boolean matches(Inventory inv, World world) {
		if (ingredient.test(inv.getStack(3))) {
			for (int i = 0; i < 3; i++) {
				if (base.test(inv.getStack(i)))
					return true;
			}
		}
		return false;
	}

	public ItemStack[] craftAll(Inventory inv) {
		ItemStack[] stacks = new ItemStack[3];

		for (int i = 0; i < 3; i++) {
			if (base.test(inv.getStack(i))) {
				int finalI = i;
				stacks[i] = RecipeUtil.applyDollars(result.copy(), resultDollars, ref -> resolveReference(inv, finalI, ref));
			}
		}
		return stacks;
	}

	@Override
	public DefaultedList<ItemStack> getRemainingStacks(Inventory inv) {
		DefaultedList<ItemStack> stacks = DefaultedList.ofSize(4, ItemStack.EMPTY);

		stacks.set(3, RecipeUtil.getRemainder(inv.getStack(3), ingredient, ref -> {
			if ("ingredient".equals(ref)) {
				return inv.getStack(3);
			}
			throw new UnresolvedDollarReferenceException(ref);
		}));

		for (int i = 0; i < 3; i++) {
			if (base.test(inv.getStack(i))) {
				int finalI = i;
				stacks.set(i, RecipeUtil.getRemainder(inv.getStack(i), base, ref -> resolveReference(inv, finalI, ref)));
			}
		}
		return stacks;
	}

	private Object resolveReference(Inventory inv, int baseIndex, String reference) throws UnresolvedDollarReferenceException {
		switch (reference) {
			case "ingredient":
				return inv.getStack(3);
			case "base":
				return inv.getStack(baseIndex);
			default:
				throw new UnresolvedDollarReferenceException(reference);
		}
	}
}
