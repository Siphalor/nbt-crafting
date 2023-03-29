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

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.ApiStatus;

import de.siphalor.nbtcrafting.NbtCrafting;

@ApiStatus.Experimental
public class WrappedRecipeSerializer implements RecipeSerializer<Recipe<?>> {
	private static long lastWarnTime = 0;

	@Override
	public Recipe<?> read(Identifier id, JsonObject json) {
		long time = System.currentTimeMillis();
		if (time > lastWarnTime + 30000) {
			NbtCrafting.logWarn("Some recipes are using the nbtcrafting:wrapped recipe type. This type is experimental and likely to change.");
		}
		lastWarnTime = time;

		JsonObject innerJson = JsonHelper.getObject(json, "recipe");
		String innerType = JsonHelper.getString(innerJson, "type");
		RecipeSerializer<?> innerSerializer = Registries.RECIPE_SERIALIZER.get(new Identifier(innerType));
		if (innerSerializer == null) {
			throw new JsonSyntaxException("Failed to resolve inner recipe type: " + innerType);
		}
		return innerSerializer.read(id, innerJson);
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
