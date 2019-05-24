package de.siphalor.nbtcrafting.client.rei;

import de.siphalor.nbtcrafting.Core;
import de.siphalor.nbtcrafting.brewing.BrewingRecipe;
import me.shedaniel.rei.api.REIPluginEntry;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.plugin.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class REIPlugin implements REIPluginEntry {
	public static final Identifier IDENTIFIER = new Identifier(Core.MODID, "rei_plugin");

	@Override
	public Identifier getPluginIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void registerRecipeDisplays(RecipeHelper recipeHelper) {
		recipeHelper.getAllSortedRecipes().forEach(recipe -> {
			if(recipe instanceof BrewingRecipe) {
                for(ItemStack stack : ((BrewingRecipe) recipe).base.getStackArray()) {
                	recipeHelper.registerDisplay(DefaultPlugin.BREWING, new DefaultBrewingDisplay(stack, ((BrewingRecipe) recipe).ingredient, recipe.getOutput()));
				}
			}
		});
	}
}
