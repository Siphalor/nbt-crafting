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
import com.google.gson.JsonObject;
import de.siphalor.nbtcrafting.util.duck.IItemStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;

public class IngredientMultiStackEntry extends IngredientEntry {

	private final IngredientEntryCondition condition;
	private final IntList itemIds;
	private String tag;

	public IngredientMultiStackEntry(Collection<Integer> items, IngredientEntryCondition condition) {
		super();
		this.condition = condition;
		this.itemIds = new IntArrayList(items);
		this.tag = "";
	}

	@Override
	public boolean matches(ItemStack stack) {
		return itemIds.contains(Registry.ITEM.getRawId(stack.getItem())) && condition.matches(stack);
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("tag", tag);
		condition.addToJson(json);
		return json;
	}

	@Override
	public Collection<ItemStack> getPreviewStacks(boolean nbt) {
		CompoundTag tag = condition.getPreviewTag();
		Collection<ItemStack> stacks = new ArrayList<>(itemIds.size());
		for (Integer id : itemIds) {
			ItemStack stack = new ItemStack(Registry.ITEM.get(id));
			stacks.add(stack);
		}
		if (nbt) {
			for (ItemStack itemStack : stacks) {
				((IItemStack) (Object) itemStack).setRawTag(tag);
			}
		}
		return stacks;
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(itemIds.size());
		for (int i = 0; i < itemIds.size(); i++) {
			buf.writeVarInt(itemIds.getInt(i));
		}
		this.condition.write(buf);
		buf.writeBoolean(remainder != null);
		if (remainder != null)
			buf.writeItemStack(remainder);
	}

	public static IngredientMultiStackEntry read(PacketByteBuf buf) {
		int length = buf.readVarInt();
		ArrayList<Integer> ids = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			ids.add(buf.readVarInt());
		}
		IngredientMultiStackEntry entry = new IngredientMultiStackEntry(ids, IngredientEntryCondition.read(buf));
		if (buf.readBoolean())
			entry.setRecipeRemainder(buf.readItemStack());
		return entry;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
