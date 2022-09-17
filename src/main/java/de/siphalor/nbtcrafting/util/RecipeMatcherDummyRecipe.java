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

package de.siphalor.nbtcrafting.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class RecipeMatcherDummyRecipe implements Recipe<Inventory> {
	private final DefaultedList<Ingredient> ingredients;

	public RecipeMatcherDummyRecipe(DefaultedList<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}

	@Override
	public DefaultedList<Ingredient> getPreviewInputs() {
		return ingredients;
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ItemStack craft(Inventory inventory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean fits(int width, int height) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ItemStack getOutput() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Identifier getId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public RecipeType<?> getType() {
		throw new UnsupportedOperationException();
	}
}
