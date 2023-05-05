/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting.compat.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import com.google.common.collect.ImmutableMap;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.type.SimpleDollar;
import de.siphalor.nbtcrafting.recipe.IngredientRecipe;

public class EmiNbtSmithingRecipe<I extends Inventory> implements EmiRecipe {
	private final Identifier identifier;
	protected final Ingredient base;
	protected final Ingredient ingredient;
	protected final ItemStack result;
	protected final Dollar[] resultDollars;
	private final int uniq = EmiUtil.RANDOM.nextInt();

	public EmiNbtSmithingRecipe(IngredientRecipe<I> recipe) {
		identifier = recipe.getId();
		base = recipe.getBase();
		ingredient = recipe.getIngredient();
		result = recipe.getOutput();
		resultDollars = recipe.resultDollars;
	}

	public @Nullable Identifier getId() {
		return identifier;
	}

	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.SMITHING;
	}

	public List<EmiIngredient> getInputs() {
		return List.of(EmiIngredient.of(this.base), EmiIngredient.of(this.ingredient));
	}

	public List<EmiStack> getOutputs() {
		return List.of(EmiStack.of(this.result));
	}

	public boolean supportsRecipeTree() {
		return false;
	}

	public int getDisplayWidth() {
		return 125;
	}

	public int getDisplayHeight() {
		return 18;
	}

	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addGeneratedSlot(r -> getTool(r, false), this.uniq, 0, 0);
		widgets.addSlot(EmiIngredient.of(this.ingredient), 49, 0);
		widgets.addGeneratedSlot(r -> getTool(r, true), this.uniq, 107, 0).recipeContext(this);
	}

	private EmiStack getTool(Random r, boolean getResult) {
		ItemStack base = this.base.getMatchingStacks()[r.nextInt(this.base.getMatchingStacks().length)];
		ItemStack ingredient = this.ingredient.getMatchingStacks()[r.nextInt(this.ingredient.getMatchingStacks().length)];
		if (base.isDamageable() && result.isDamageable() && Arrays.stream(resultDollars).anyMatch(d -> d instanceof SimpleDollar sd && Objects.equals(sd.path, "Damage"))) {
			base.setDamage(r.nextInt(base.getMaxDamage()));
		}
		return getResult ? EmiStack.of(RecipeUtil.applyDollars(this.result.copy(), resultDollars, ImmutableMap.of("base", NbtUtil.getTagOrEmpty(base), "ingredient", NbtUtil.getTagOrEmpty(ingredient)))) : EmiStack.of(base);
	}
}

