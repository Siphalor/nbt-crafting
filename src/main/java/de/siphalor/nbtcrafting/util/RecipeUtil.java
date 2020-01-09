package de.siphalor.nbtcrafting.util;

import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.util.nbt.NbtHelper;
import de.siphalor.nbtcrafting.util.nbt.NbtIterator;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RecipeUtil {
	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, DefaultedList<Ingredient> ingredients, Inventory inventory) {
        ItemStack stack = baseOutput.copy();
		Dollar[] dollars = DollarParser.extractDollars(stack.getTag(), true);

		if(dollars.length > 0) {
			Map<String, Object> reference = new HashMap<>();
			ingredient:
			for (int j = 0; j < ingredients.size(); j++) {
				for (int i = 0; i < inventory.getInvSize(); i++) {
					if(ingredients.get(j).test(inventory.getInvStack(i))) {
						reference.putIfAbsent("i" + j, NbtHelper.getTagOrEmpty(inventory.getInvStack(i)));
						continue ingredient;
					}
				}
			}

			return applyDollars(stack, dollars, reference);
		}
		return null;
	}

	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, Ingredient ingredient, Inventory inventory) {
		return getDollarAppliedOutputStack(baseOutput, ingredient, "this", inventory);
	}

	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, Ingredient ingredient, String referenceName, Inventory inventory) {
		Dollar[] dollars = DollarParser.extractDollars(baseOutput.getTag(), true);

		if(dollars.length > 0) {
			ItemStack stack = baseOutput.copy();

			Map<String, Object> reference = new HashMap<>();
			reference.put(referenceName, NbtHelper.getTagOrEmpty(inventory.getInvStack(0)));

			return applyDollars(stack, dollars, reference);
		}
		return null;
	}

	public static ItemStack applyDollars(ItemStack stack, Dollar[] dollars, Map<String, Object> reference) {
		Arrays.stream(dollars).forEach(dollar -> {
			try {
				dollar.apply(stack, reference);
			} catch (DollarException e) {
				e.printStackTrace();
			}
		});
		NbtIterator.iterateTags(NbtHelper.getTagOrEmpty(stack), (path, key, tag) -> key.equals("$overwrite") || tag.asString().equals("$overwrite"));
		if(stack.getDamage() > stack.getMaxDamage()) {
			return ItemStack.EMPTY;
		}
		return stack;
	}
}
