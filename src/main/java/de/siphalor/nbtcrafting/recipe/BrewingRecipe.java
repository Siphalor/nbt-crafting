package de.siphalor.nbtcrafting.recipe;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.api.nbt.NbtHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class BrewingRecipe extends IngredientRecipe<Inventory> implements ServerRecipe {
	public static final RecipeSerializer<BrewingRecipe> SERIALIZER = new IngredientRecipe.Serializer<>(BrewingRecipe::new);

	public BrewingRecipe(Identifier identifier, Ingredient base, Ingredient ingredient, ItemStack result, Serializer<BrewingRecipe> serializer) {
		super(identifier, base, ingredient, result, NbtCrafting.BREWING_RECIPE_TYPE, serializer);
	}

	@Override
	public boolean matches(Inventory inv, World world) {
		if (ingredient.test(inv.getStack(3))) {
			for (int i = 0; i < 3; i++) {
				if (base.test(inv.getStack(i)))
					return true;
			}
		}
		return false;
	}

	public ItemStack[] craftAll(Inventory inv) {
		ItemStack[] stacks = new ItemStack[3];

		Map<String, Object> reference = new HashMap<>();
		reference.put("ingredient", NbtHelper.getTagOrEmpty(inv.getStack(3)));

		for (int i = 0; i < 3; i++) {
			if (base.test(inv.getStack(i))) {
				reference.put("base", NbtHelper.getTagOrEmpty(inv.getStack(i)));
				stacks[i] = RecipeUtil.applyDollars(result.copy(), resultDollars, reference);
			}
		}
		return stacks;
	}

	@Override
	public DefaultedList<ItemStack> getRemainingStacks(Inventory inv) {
		DefaultedList<ItemStack> stacks = DefaultedList.ofSize(4, ItemStack.EMPTY);
		Map<String, Object> reference = new HashMap<>();
		reference.put("ingredient", inv.getStack(3));
		stacks.set(3, RecipeUtil.getRemainder(inv.getStack(3), ingredient, reference));

		for (int i = 0; i < 3; i++) {
			if (base.test(inv.getStack(i))) {
				reference.put("base", inv.getStack(i));
				stacks.set(i, RecipeUtil.getRemainder(inv.getStack(i), base, reference));
			}
		}
		return stacks;
	}
}
