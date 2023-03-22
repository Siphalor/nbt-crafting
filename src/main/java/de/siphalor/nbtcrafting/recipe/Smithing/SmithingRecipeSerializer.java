package de.siphalor.nbtcrafting.recipe.Smithing;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SmithingRecipeSerializer implements RecipeSerializer<SmithingRecipe> {
	@Override
	public SmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
		JsonObject output = JsonHelper.getObject(jsonObject,"result");
		return new SmithingRecipe(identifier, Ingredient.fromJson(jsonObject.get("base")), Ingredient.fromJson(jsonObject.get("template")), Ingredient.fromJson(jsonObject.get("addition")), ShapedRecipe.outputFromJson(output));
	}

	@Override
	public SmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
		return SmithingRecipe.from(packetByteBuf);
	}

	@Override
	public void write(PacketByteBuf packetByteBuf, SmithingRecipe recipe) {
		recipe.write(packetByteBuf);
	}
}
