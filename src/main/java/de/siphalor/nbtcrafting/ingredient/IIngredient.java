package de.siphalor.nbtcrafting.ingredient;

import net.minecraft.item.ItemStack;

import java.util.stream.Stream;

public interface IIngredient {
	
	void setRealEntries(Stream<? extends IngredientEntry> entries);

	ItemStack getRecipeRemainder(ItemStack stack);
}
