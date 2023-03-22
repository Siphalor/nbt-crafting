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

package de.siphalor.nbtcrafting.recipe;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;

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

		Map<String, Object> reference = new HashMap<>();
		reference.put("ingredient", NbtUtil.getTagOrEmpty(inv.getStack(3)));

		for (int i = 0; i < 3; i++) {
			if (base.test(inv.getStack(i))) {
				reference.put("base", NbtUtil.getTagOrEmpty(inv.getStack(i)));
				stacks[i] = RecipeUtil.applyDollars(result.copy(), resultDollars, reference);
			}
		}
		return stacks;
	}

	@Override
	public DefaultedList<ItemStack> getRemainder(Inventory inv) {
		DefaultedList<ItemStack> stacks = DefaultedList.ofSize(4, ItemStack.EMPTY);
		Map<String, Object> reference = new HashMap<>();
		reference.put("ingredient", NbtUtil.getTagOrEmpty(inv.getStack(3)));
		stacks.set(3, RecipeUtil.getRemainder(inv.getStack(3), ingredient, reference));

		for (int i = 0; i < 3; i++) {
			if (base.test(inv.getStack(i))) {
				reference.put("base", NbtUtil.getTagOrEmpty(inv.getStack(i)));
				stacks.set(i, RecipeUtil.getRemainder(inv.getStack(i), base, reference));
			}
		}
		return stacks;
	}

	public ItemStack getOutput() {
		return this.result;
	}
}
