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

package de.siphalor.nbtcrafting3.recipe.cauldron;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting3.NbtCrafting;
import de.siphalor.nbtcrafting3.api.RecipeUtil;
import de.siphalor.nbtcrafting3.api.ServerRecipe;
import de.siphalor.nbtcrafting3.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting3.dollar.Dollar;
import de.siphalor.nbtcrafting3.dollar.DollarExtractor;
import de.siphalor.nbtcrafting3.dollar.exception.UnresolvedDollarReferenceException;
import de.siphalor.nbtcrafting3.dollar.reference.ReferenceResolver;

public class CauldronRecipe implements NBTCRecipe<TemporaryCauldronInventory>, ServerRecipe {
	private final Identifier identifier;
	public final Ingredient input;
	public final ItemStack output;
	public final int levels;
	private final Dollar[] outputDollars;

	public CauldronRecipe(Identifier id, Ingredient ingredient, ItemStack output, int levels) {
		this.identifier = id;
		this.input = ingredient;
		this.output = output;
		this.levels = levels;
		this.outputDollars = DollarExtractor.extractDollars(output.getTag(), false);
	}

	public void write(PacketByteBuf packetByteBuf) {
		packetByteBuf.writeIdentifier(identifier);
		input.write(packetByteBuf);
		packetByteBuf.writeItemStack(output);
		packetByteBuf.writeShort(levels);
	}

	public static CauldronRecipe from(PacketByteBuf packetByteBuf) {
		Identifier identifier = packetByteBuf.readIdentifier();
		Ingredient input = Ingredient.fromPacket(packetByteBuf);
		ItemStack output = packetByteBuf.readItemStack();
		int levels = packetByteBuf.readShort();
		return new CauldronRecipe(identifier, input, output, levels);
	}

	@Override
	public boolean matches(TemporaryCauldronInventory inventory, World world) {
		if (!input.test(inventory.getInvStack(0))) {
			return false;
		}
		if (levels >= 0) {
			return inventory.getLevel() >= levels;
		} else {
			return inventory.getMaxLevel() - inventory.getLevel() >= -levels;
		}
	}

	@Override
	public ItemStack craft(TemporaryCauldronInventory inventory) {
		int level = inventory.getLevel() - levels;

		ItemStack result = RecipeUtil.applyDollars(output.copy(), outputDollars, ref -> {
			switch (ref) {
				case "ingredient":
					return inventory.getInvStack(0);
				case "oldLevel":
					return inventory.getLevel();
				case "newLevel":
					return level;
				default:
					throw new UnresolvedDollarReferenceException(ref);
			}
		});

		inventory.setLevel(level);
		inventory.getInvStack(0).decrement(1);
		return result;
	}

	@Override
	public boolean fits(int i, int i1) {
		return false;
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public DefaultedList<Ingredient> getPreviewInputs() {
		return DefaultedList.copyOf(Ingredient.EMPTY, input);
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NbtCrafting.CAULDRON_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return NbtCrafting.CAULDRON_RECIPE_TYPE;
	}

	@Override
	public ReferenceResolver getReferenceResolver(TemporaryCauldronInventory inv) {
		return ref -> {
			if ("ingredient".equals(ref)) {
				return inv.getInvStack(0);
			}
			throw new UnresolvedDollarReferenceException(ref);
		};
	}
}