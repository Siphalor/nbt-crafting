package de.siphalor.nbtcrafting.recipe;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class BrewingRecipe extends IngredientRecipe<Inventory> implements ServerRecipe {
	public static final RecipeSerializer<BrewingRecipe> SERIALIZER = new IngredientRecipe.Serializer<>(BrewingRecipe::new);

	public BrewingRecipe(Identifier identifier, Ingredient base, Ingredient ingredient, ItemStack result) {
		super(identifier, base, ingredient, result);
	}

	@Override
	public boolean matches(Inventory inv, World world) {
		if (ingredient.test(inv.getInvStack(3))) {
			for (int i = 0; i < 3; i++) {
				if (base.test(inv.getInvStack(i)))
					return true;
			}
		}
		return false;
	}

	public ItemStack[] craftAll(Inventory inv) {
		ItemStack[] stacks = new ItemStack[3];

		Map<String, Object> reference = new HashMap<>();
		reference.put("ingredient", NbtUtil.getTagOrEmpty(inv.getInvStack(3)));

		for (int i = 0; i < 3; i++) {
			if (base.test(inv.getInvStack(i))) {
				reference.put("base", NbtUtil.getTagOrEmpty(inv.getInvStack(i)));
				stacks[i] = RecipeUtil.applyDollars(result.copy(), resultDollars, reference);
			}
		}
		return stacks;
	}

	@Override
	public DefaultedList<ItemStack> getRemainingStacks(Inventory inv) {
		DefaultedList<ItemStack> stacks = DefaultedList.ofSize(4, ItemStack.EMPTY);
		Map<String, Object> reference = new HashMap<>();
		reference.put("ingredient", inv.getInvStack(3));
		stacks.set(3, RecipeUtil.getRemainder(inv.getInvStack(3), ingredient, reference));

		for (int i = 0; i < 3; i++) {
			if (base.test(inv.getInvStack(i))) {
				reference.put("base", inv.getInvStack(i));
				stacks.set(i, RecipeUtil.getRemainder(inv.getInvStack(i), base, reference));
			}
		}
		return stacks;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return NbtCrafting.BREWING_RECIPE_TYPE;
	}
}
