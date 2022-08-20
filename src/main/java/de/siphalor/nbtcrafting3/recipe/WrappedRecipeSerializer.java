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

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import de.siphalor.nbtcrafting3.NbtCrafting;

@ApiStatus.Experimental
public class WrappedRecipeSerializer implements RecipeSerializer<Recipe<?>> {
	@Override
	public Recipe<?> read(Identifier id, JsonObject json) {
		NbtCrafting.advancedIngredientSerializationEnabled.set(true);
		JsonObject innerJson = JsonHelper.getObject(json, "recipe");
		String innerType = JsonHelper.getString(innerJson, "type");
		RecipeSerializer<?> innerSerializer = Registry.RECIPE_SERIALIZER.get(new Identifier(innerType));
		if (innerSerializer == null) {
			throw new JsonSyntaxException("Failed to resolve inner recipe type: " + innerType);
		}
		Recipe<?> recipe = innerSerializer.read(id, innerJson);
		NbtCrafting.advancedIngredientSerializationEnabled.set(false);
		return recipe;
	}

	@Override
	public Recipe<?> read(Identifier id, PacketByteBuf buf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(PacketByteBuf buf, Recipe<?> recipe) {
		throw new UnsupportedOperationException();
	}
}
