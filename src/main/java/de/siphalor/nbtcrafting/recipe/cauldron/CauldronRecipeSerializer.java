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
		JsonObject input = JsonHelper.getObject(jsonObject, "input");
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
		return new CauldronRecipe(identifier, Ingredient.fromJson(input), ShapedRecipe.getItemStack(output), fluid, levels);
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
