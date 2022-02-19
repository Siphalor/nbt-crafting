package de.siphalor.nbtcrafting.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class WrappedRecipeSerializer implements RecipeSerializer<Recipe<?>> {
	@Override
	public Recipe<?> read(Identifier id, JsonObject json) {
		JsonObject innerJson = JsonHelper.getObject(json, "recipe");
		String innerType = JsonHelper.getString(innerJson, "type");
		RecipeSerializer<?> innerSerializer = Registry.RECIPE_SERIALIZER.get(new Identifier(innerType));
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
