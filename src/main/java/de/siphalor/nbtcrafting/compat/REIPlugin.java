/*
 * Copyright 2020-2021 Siphalor
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

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.recipe.BrewingRecipe;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class REIPlugin implements REIPluginV0 {
	public static final Identifier IDENTIFIER = new Identifier(NbtCrafting.MOD_ID, "rei_plugin");

	@Override
	public Identifier getPluginIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void registerRecipeDisplays(RecipeHelper recipeHelper) {
		recipeHelper.getAllSortedRecipes().forEach(recipe -> {
			if(recipe instanceof BrewingRecipe) {
				recipeHelper.registerDisplay(new DefaultBrewingDisplay(((BrewingRecipe) recipe).getBase(), ((BrewingRecipe) recipe).getIngredient(), recipe.getOutput()));
			}
		});
	}
}
