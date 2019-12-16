package de.siphalor.nbtcrafting.util;

import de.siphalor.nbtcrafting.dollars.Dollar;
import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarParser;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;

import java.util.Arrays;
import java.util.HashMap;

public class RecipeUtil {
	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, DefaultedList<Ingredient> ingredients, Inventory inventory) {
        ItemStack stack = baseOutput.copy();
		Dollar[] dollars = DollarParser.extractDollars(stack.getOrCreateTag());

		if(dollars.length > 0) {
			HashMap<String, CompoundTag> reference = new HashMap<>();
			ingredient:
			for (int j = 0; j < ingredients.size(); j++) {
				for (int i = 0; i < inventory.getInvSize(); i++) {
					if(ingredients.get(j).test(inventory.getInvStack(i))) {
						reference.putIfAbsent("i" + j, inventory.getInvStack(i).getOrCreateTag().copy());
						continue ingredient;
					}
				}
			}

			applyDollars(stack, dollars, reference);

            return stack;
		}
		return null;
	}

	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, Ingredient ingredient, Inventory inventory) {
		Dollar[] dollars = DollarParser.extractDollars(baseOutput.getOrCreateTag());

		if(dollars.length > 0) {
			ItemStack stack = baseOutput.copy();

			HashMap<String, CompoundTag> reference = new HashMap<>();
			reference.put("i0", inventory.getInvStack(0).getOrCreateTag());

			applyDollars(stack, dollars, reference);

			return stack;
		}
		return null;
	}

	private static void applyDollars(ItemStack stack, Dollar[] dollars, HashMap<String, CompoundTag> reference) {
		Arrays.stream(dollars).forEach(dollar -> {
			try {
				dollar.apply(stack, reference);
			} catch (DollarException e) {
				e.printStackTrace();
			}
		});
	}
}
