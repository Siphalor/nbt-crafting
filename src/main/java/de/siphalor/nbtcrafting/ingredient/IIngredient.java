package de.siphalor.nbtcrafting.ingredient;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.stream.Stream;

public interface IIngredient {
	
	void setAdvancedEntries(Stream<? extends IngredientEntry> entries);


	ItemStack getRecipeRemainder(ItemStack stack, HashMap<String, CompoundTag> reference);
}
