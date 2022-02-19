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

package de.siphalor.nbtcrafting.ingredient;

import java.util.Collection;
import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import de.siphalor.nbtcrafting.util.duck.IItemStack;

public class IngredientStackEntry extends IngredientEntry {

	private final IngredientEntryCondition condition;
	private final int id;

	public IngredientStackEntry(int id, IngredientEntryCondition condition) {
		super();
		this.id = id;
		this.condition = condition;
	}

	public IngredientStackEntry(ItemStack stack) {
		this.id = Registry.ITEM.getRawId(stack.getItem());
		if (stack.hasTag())
			this.condition = new IngredientEntryCondition(stack.getTag(), new CompoundTag());
		else
			this.condition = new IngredientEntryCondition();
	}

	@Override
	public boolean matches(ItemStack stack) {
		return Registry.ITEM.getRawId(stack.getItem()) == this.id && condition.matches(stack);
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("item", Registry.ITEM.getId(Registry.ITEM.get(id)).toString());
		condition.addToJson(json);
		return json;
	}

	@Override
	public Collection<ItemStack> getPreviewStacks(boolean nbt) {
		ItemStack stack = new ItemStack(Registry.ITEM.get(id));
		if (nbt) {
			((IItemStack) (Object) stack).nbtCrafting$setRawTag(condition.getPreviewTag());
		}
		return Collections.singleton(stack);
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(id);
		this.condition.write(buf);
		buf.writeBoolean(remainder != null);
		if (remainder != null)
			buf.writeItemStack(remainder);
	}

	public static IngredientStackEntry read(PacketByteBuf buf) {
		IngredientStackEntry entry = new IngredientStackEntry(buf.readVarInt(), IngredientEntryCondition.read(buf));
		if (buf.readBoolean())
			entry.setRecipeRemainder(buf.readItemStack());
		return entry;
	}

}
