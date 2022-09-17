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

package de.siphalor.nbtcrafting3.compat;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import de.siphalor.nbtcrafting3.recipe.BrewingRecipe;

@Environment(EnvType.CLIENT)
public class REIPlugin implements REIClientPlugin {
	@Override
	public void registerDisplays(DisplayRegistry registry) {
		registry.registerFiller(BrewingRecipe.class, recipe -> {
			return new DefaultBrewingDisplay(recipe.getBase(), recipe.getIngredient(), recipe.getOutput());
		});
	}
}
