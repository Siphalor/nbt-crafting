/*
 * Copyright 2020-2021 Siphalor
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

package de.siphalor.nbtcrafting.ingredient;

import com.google.gson.JsonElement;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import java.util.Collection;
import java.util.Map;

public abstract class IngredientEntry {
	protected ItemStack remainder;
	protected Dollar[] remainderDollars;

	public IngredientEntry() {
		this.remainder = null;
		this.remainderDollars = new Dollar[0];
	}

	public abstract boolean matches(ItemStack stack);

	public abstract JsonElement toJson();

	public Collection<ItemStack> getPreviewStacks() {
		return getPreviewStacks(true);
	}

	public abstract Collection<ItemStack> getPreviewStacks(boolean nbt);

	public abstract void write(PacketByteBuf buf);

	public ItemStack getRecipeRemainder(ItemStack stack, Map<String, Object> reference) {
		if (remainder == null)
			return ItemStack.EMPTY;
		return RecipeUtil.applyDollars(remainder.copy(), remainderDollars, reference);
	}

	public void setRecipeRemainder(ItemStack stack) {
		this.remainder = stack;
		if (stack.hasTag())
			this.remainderDollars = DollarParser.extractDollars(stack.getTag(), true);
	}
}
