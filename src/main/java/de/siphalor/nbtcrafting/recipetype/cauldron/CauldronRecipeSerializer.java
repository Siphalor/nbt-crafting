package de.siphalor.nbtcrafting.recipetype.cauldron;

import com.google.gson.JsonObject;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;

public class CauldronRecipeSerializer implements RecipeSerializer<CauldronRecipe> {
	@Override
	public CauldronRecipe read(Identifier identifier, JsonObject jsonObject) {
		JsonObject input = JsonHelper.getObject(jsonObject, "input");
		JsonObject output = JsonHelper.getObject(jsonObject, "result");
		return new CauldronRecipe(identifier, Ingredient.fromJson(input), ShapedRecipe.getItemStack(output), JsonHelper.getInt(jsonObject, "levels", 0));
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
