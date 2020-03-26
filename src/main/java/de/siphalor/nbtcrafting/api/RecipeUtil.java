package de.siphalor.nbtcrafting.api;

import de.siphalor.nbtcrafting.api.nbt.NbtHelper;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.ingredient.IIngredient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RecipeUtil {
	@Deprecated
	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, DefaultedList<Ingredient> ingredients, Inventory inventory) {
		return getDollarAppliedResult(baseOutput, ingredients, inventory);
	}

	public static ItemStack getDollarAppliedResult(ItemStack baseOutput, DefaultedList<Ingredient> ingredients, Inventory inventory) {
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
		return stack;
	}

	@Deprecated
	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, Ingredient ingredient, Inventory inventory) {
		return getDollarAppliedResult(baseOutput, ingredient, inventory);
	}

	public static ItemStack getDollarAppliedResult(ItemStack baseOutput, Ingredient ingredient, Inventory inventory) {
		return getDollarAppliedResult(baseOutput, ingredient, "this", inventory);
	}

	@Deprecated
	public static ItemStack getDollarAppliedOutputStack(ItemStack baseOutput, Ingredient ingredient, String referenceName, Inventory inventory) {
		return getDollarAppliedResult(baseOutput, ingredient, referenceName, inventory);
	}

	public static ItemStack getDollarAppliedResult(ItemStack baseOutput, Ingredient ingredient, String referenceName, Inventory inventory) {
		ItemStack stack = baseOutput.copy();
		Dollar[] dollars = DollarParser.extractDollars(stack.getTag(), true);

		if(dollars.length > 0) {
			Map<String, Object> reference = new HashMap<>();
			reference.put(referenceName, NbtHelper.getTagOrEmpty(inventory.getInvStack(0)));

			return applyDollars(stack, dollars, reference);
		}
		return stack;
	}

	public static ItemStack getRemainder(ItemStack itemStack, Ingredient ingredient, Map<String, Object> reference) {
		ItemStack result = ((IIngredient)(Object) ingredient).getRecipeRemainder(itemStack, reference);
		if (result == null) {
			return new ItemStack(itemStack.getItem().getRecipeRemainder());
		}
		return result;
	}

	public static void putRemainders(DefaultedList<ItemStack> remainders, Inventory target, World world, BlockPos scatterPos) {
		putRemainders(remainders, target, world, scatterPos, 0);
	}

	public static void putRemainders(DefaultedList<ItemStack> remainders, Inventory target, World world, BlockPos scatterPos, int offset) {
		final int size = remainders.size();
		if (size > target.getInvSize()) {
			throw new IllegalArgumentException("Size of given remainder list must be <= size of target inventory");
		}
		for (int i = 0; i < size; i++) {
			if (target.getInvStack(offset + i).isEmpty()) {
				target.setInvStack(offset + i, remainders.get(i));
				remainders.set(i, ItemStack.EMPTY);
			}
		}
		ItemScatterer.spawn(world, scatterPos, remainders);
	}

	public static ItemStack applyDollars(ItemStack stack, Dollar[] dollars, Map<String, Object> reference) {
		Arrays.stream(dollars).forEach(dollar -> {
			try {
				dollar.apply(stack, reference);
			} catch (DollarException e) {
				e.printStackTrace();
			}
		});
		if(stack.getDamage() > stack.getMaxDamage()) {
			return ItemStack.EMPTY;
		}
		return stack;
	}
}
