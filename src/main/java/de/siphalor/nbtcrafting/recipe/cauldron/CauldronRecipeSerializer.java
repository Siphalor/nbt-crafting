/*
 * Copyright 2020 Siphalor
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

package de.siphalor.nbtcrafting.recipe.cauldron;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CauldronRecipeSerializer implements RecipeSerializer<CauldronRecipe> {
	@Override
	public CauldronRecipe read(Identifier identifier, JsonObject jsonObject) {
		JsonObject output = JsonHelper.getObject(jsonObject, "result");
		int levels = 0;
		Identifier fluid = null;
		if (jsonObject.has("levels")) {
			levels = jsonObject.get("levels").getAsInt();
			if (jsonObject.has("fluid")) {
				fluid = new Identifier(jsonObject.get("fluid").getAsString());
			} else {
				fluid = TemporaryCauldronInventory.WATER;
			}
		}
		return new CauldronRecipe(identifier, Ingredient.fromJson(jsonObject.get("input")), ShapedRecipe.getItemStack(output), fluid, levels);
	}

	@Override
	public CauldronRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
		return CauldronRecipe.from(packetByteBuf);
	}

	@Override
	public void write(PacketByteBuf packetByteBuf, CauldronRecipe cauldronRecipe) {
		cauldronRecipe.write(packetByteBuf);
	}
}
