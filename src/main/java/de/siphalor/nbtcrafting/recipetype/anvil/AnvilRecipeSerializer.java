package de.siphalor.nbtcrafting.recipetype.anvil;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;

public class AnvilRecipeSerializer implements RecipeSerializer<AnvilRecipe> {
	@Override
	public AnvilRecipe read(Identifier identifier, JsonObject jsonObject) {
		Ingredient base = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "base"));
		Ingredient ingredient = null;
		if(jsonObject.has("ingredient")) {
			ingredient = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "ingredient"));
		}
		ItemStack output = ShapedRecipe.getItemStack(JsonHelper.getObject(jsonObject, "result"));
		int levels = JsonHelper.getInt(jsonObject, "levels", 0);
		return new AnvilRecipe(identifier, base, ingredient, output, levels);
	}

	@Override
	public AnvilRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
		return AnvilRecipe.from(packetByteBuf);
	}

	@Override
	public void write(PacketByteBuf packetByteBuf, AnvilRecipe anvilRecipe) {
		anvilRecipe.write(packetByteBuf);
	}
}
