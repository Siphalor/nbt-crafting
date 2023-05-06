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

package de.siphalor.nbtcrafting.compat.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.SmithingRecipe;

import de.siphalor.nbtcrafting.recipe.IngredientRecipe;

public class EmiNbtSmithingRecipe<I extends Inventory> extends EmiSmithingRecipe implements EmiRecipe {
	public EmiNbtSmithingRecipe(IngredientRecipe<?> recipe) {
		super(new SmithingRecipe(recipe.getId(), recipe.getBase(), recipe.getIngredient(), recipe.getOutput()));
	}
}

