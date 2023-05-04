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

package de.siphalor.nbtcrafting.compat;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.SmithingRecipe;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.recipe.IngredientRecipe;

public class EMIPlugin implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		for (IngredientRecipe<Inventory> recipe : registry.getRecipeManager().listAllOfType(NbtCrafting.SMITHING_RECIPE_TYPE)) {
			registry.addRecipe(new EmiSmithingRecipe(new SmithingRecipe(recipe.getId(), recipe.getBase(), recipe.getIngredient(), recipe.getOutput())));
		}
		for (IngredientRecipe<Inventory> recipe : registry.getRecipeManager().listAllOfType(NbtCrafting.BREWING_RECIPE_TYPE)) {
			if (recipe.getBase().getMatchingStacks().length != 0) {
				registry.addRecipe(new EmiBrewingRecipe(EmiStack.of(recipe.getBase().getMatchingStacks()[0]), EmiIngredient.of(recipe.getIngredient()), EmiStack.of(recipe.getOutput()), recipe.getId()));
			}
		}
	}
}
