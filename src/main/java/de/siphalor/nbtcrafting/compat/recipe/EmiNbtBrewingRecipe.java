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

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.recipe.BrewingRecipe;

public class EmiNbtBrewingRecipe implements EmiRecipe {
	private static final Identifier BACKGROUND = new Identifier("minecraft", "textures/gui/container/brewing_stand.png");
	private static final EmiStack BLAZE_POWDER = EmiStack.of(Items.BLAZE_POWDER);
	private final EmiIngredient input;
	private final EmiIngredient ingredient;
	private final EmiStack output;
	private final Identifier id;

	public EmiNbtBrewingRecipe(BrewingRecipe recipe) {
		this.input = EmiIngredient.of(Arrays.stream(recipe.getBase().getMatchingStacks()).map(EmiStack::of).toList());
		this.ingredient = EmiIngredient.of(recipe.getIngredient());
		this.output = EmiStack.of(recipe.getOutput());
		this.id = recipe.getId();
	}

	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.BREWING;
	}

	@Nullable
	public Identifier getId() {
		return this.id;
	}

	public List<EmiIngredient> getInputs() {
		return List.of(this.input, this.ingredient);
	}

	public List<EmiStack> getOutputs() {
		return List.of(this.output);
	}

	public int getDisplayWidth() {
		return 120;
	}

	public int getDisplayHeight() {
		return 61;
	}

	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(BACKGROUND, 0, 0, 103, 61, 16, 14);
		widgets.addAnimatedTexture(BACKGROUND, 81, 2, 9, 28, 176, 0, 20000, false, false, false)
				.tooltip((mx, my) -> List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.cooking.time", 20)))));
		widgets.addAnimatedTexture(BACKGROUND, 47, 0, 12, 29, 185, 0, 700, false, true, false);
		widgets.addTexture(BACKGROUND, 44, 30, 18, 4, 176, 29);
		widgets.addSlot(BLAZE_POWDER, 0, 2).drawBack(false);
		widgets.addSlot(this.input, 39, 36).drawBack(false);
		widgets.addSlot(this.ingredient, 62, 2).drawBack(false);
		widgets.addSlot(this.output, 85, 36).drawBack(false).recipeContext(this);
	}
}
