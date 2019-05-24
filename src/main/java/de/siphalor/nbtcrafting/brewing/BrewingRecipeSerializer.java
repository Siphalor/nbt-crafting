package de.siphalor.nbtcrafting.brewing;

import com.google.gson.JsonObject;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;

public class BrewingRecipeSerializer implements RecipeSerializer<BrewingRecipe> {

	@Override
	public BrewingRecipe read(Identifier identifier, JsonObject json) {
		JsonObject base = JsonHelper.getObject(json, "base");
		JsonObject ingredient = JsonHelper.getObject(json, "ingredient");
		JsonObject result = JsonHelper.getObject(json, "result");
		return new BrewingRecipe(identifier, Ingredient.fromJson(base), Ingredient.fromJson(ingredient), ShapedRecipe.getItemStack(result));
	}

	@Override
	public BrewingRecipe read(Identifier var1, PacketByteBuf buffer) {
		return BrewingRecipe.from(buffer);
	}

	@Override
	public void write(PacketByteBuf buffer, BrewingRecipe brewingRecipe) {
		brewingRecipe.write(buffer);
	}
}
