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

package de.siphalor.nbtcrafting.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarExtractor;
import de.siphalor.nbtcrafting.dollar.exception.UnresolvedDollarReferenceException;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public abstract class IngredientRecipe<I extends Inventory> implements NBTCRecipe<I>, ServerRecipe {
	private final Identifier identifier;
	protected final Ingredient base;
	protected final Ingredient ingredient;
	protected final ItemStack result;
	protected final Dollar[] resultDollars;

	public IngredientRecipe(Identifier identifier, Ingredient base, Ingredient ingredient, ItemStack result) {
		this.identifier = identifier;
		this.base = base;
		this.ingredient = ingredient;
		this.result = result;
		this.resultDollars = DollarExtractor.extractDollars(result.getTag(), false);
	}

	@Override
	public boolean matches(I inv, World world) {
		if (ingredient != null && ingredient.test(inv.getInvStack(1))) {
			return base.test(inv.getInvStack(0));
		}
		return false;
	}

	@Override
	public boolean fits(int width, int height) {
		return false;
	}

	@Override
	public ItemStack craft(I inv) {
		return RecipeUtil.applyDollars(result.copy(), resultDollars, getReferenceResolver(inv));
	}

	@Override
	public ItemStack getOutput() {
		return result;
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	public Ingredient getBase() {
		return base;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	@Override
	public DefaultedList<Ingredient> getPreviewInputs() {
		return DefaultedList.copyOf(Ingredient.EMPTY, base, ingredient);
	}

	@Override
	public ReferenceResolver getReferenceResolver(I inv) {
		return ref -> {
			switch (ref) {
				case "base":
					return inv.getInvStack(0);
				case "ingredient":
					return inv.getInvStack(1);
				default:
					throw new UnresolvedDollarReferenceException(ref);
			}
		};
	}

	public void readCustomData(JsonObject json) {
	}

	public void readCustomData(PacketByteBuf buf) {
	}

	public void writeCustomData(PacketByteBuf buf) {
	}

	public interface Factory<R extends IngredientRecipe<?>> {
		R create(Identifier id, Ingredient base, Ingredient ingredient, ItemStack result);
	}

	public static class Serializer<R extends IngredientRecipe<?>> implements RecipeSerializer<R> {
		private final Factory<R> factory;

		public Serializer(Factory<R> factory) {
			this.factory = factory;
		}

		@Override
		public R read(Identifier id, JsonObject json) {
			Ingredient base = Ingredient.fromJson(json.get("base"));
			Ingredient ingredient;
			if (json.has("ingredient")) {
				ingredient = Ingredient.fromJson(json.get("ingredient"));
			} else {
				ingredient = Ingredient.EMPTY;
			}
			ItemStack result = ShapedRecipe.getItemStack(JsonHelper.getObject(json, "result"));
			R recipe = factory.create(id, base, ingredient, result);
			recipe.readCustomData(json);
			return recipe;
		}

		@Override
		public R read(Identifier id, PacketByteBuf buf) {
			Ingredient base = Ingredient.fromPacket(buf);
			Ingredient ingredient = Ingredient.fromPacket(buf);
			ItemStack result = buf.readItemStack();
			R recipe = factory.create(id, base, ingredient, result);
			recipe.readCustomData(buf);
			return recipe;
		}

		@Override
		public void write(PacketByteBuf buf, R recipe) {
			recipe.base.write(buf);
			recipe.ingredient.write(buf);
			buf.writeItemStack(recipe.result);
			recipe.writeCustomData(buf);
		}
	}
}
