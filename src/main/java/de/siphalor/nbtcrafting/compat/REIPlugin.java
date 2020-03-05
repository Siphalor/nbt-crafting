package de.siphalor.nbtcrafting.compat;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.brewing.BrewingRecipe;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.brewing.DefaultBrewingDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class REIPlugin implements REIPluginV0 {
	public static final Identifier IDENTIFIER = new Identifier(NbtCrafting.MOD_ID, "rei_plugin");

	@Override
	public Identifier getPluginIdentifier() {
		return IDENTIFIER;
	}
	
	@Override
	public SemanticVersion getMinimumVersion() throws VersionParsingException {
		return SemanticVersion.parse("3.0-pre");
	}
	
	@Override
	public void registerRecipeDisplays(RecipeHelper recipeHelper) {
		recipeHelper.getAllSortedRecipes().forEach(recipe -> {
			if(recipe instanceof BrewingRecipe) {
                for(ItemStack stack : ((BrewingRecipe) recipe).base.getMatchingStacksClient()) {
                	recipeHelper.registerDisplay(new DefaultBrewingDisplay(stack, ((BrewingRecipe) recipe).ingredient, recipe.getOutput()));
				}
			}
		});
	}
}
