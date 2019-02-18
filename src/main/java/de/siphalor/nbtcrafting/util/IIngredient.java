package de.siphalor.nbtcrafting.util;

import java.util.stream.Stream;

import de.siphalor.nbtcrafting.ingredient.IngredientEntry;

public interface IIngredient {
	
	void setRealEntries(Stream<? extends IngredientEntry> entries);
}
