package de.siphalor.nbtcrafting.util.duck;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface IRecipeManager {
	<C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> nbtCrafting$getAllOfType(RecipeType<T> type);
}
