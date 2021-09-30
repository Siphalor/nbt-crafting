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

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;

public class IngredientEntryCondition {
	public static final IngredientEntryCondition EMPTY = new IngredientEntryCondition(NbtUtil.EMPTY_COMPOUND, NbtUtil.EMPTY_COMPOUND);

	public NbtCompound requiredElements;
	public NbtCompound deniedElements;

	public IngredientEntryCondition() {
		requiredElements = NbtUtil.EMPTY_COMPOUND;
		deniedElements = NbtUtil.EMPTY_COMPOUND;
	}

	public IngredientEntryCondition(NbtCompound requiredElements, NbtCompound deniedElements) {
		this.requiredElements = requiredElements;
		this.deniedElements = deniedElements;
	}

	public boolean matches(ItemStack stack) {
		if (!stack.hasTag()) {
			return requiredElements.isEmpty();
		}
		NbtCompound tag = stack.getTag();
		//noinspection ConstantConditions
		if (!deniedElements.isEmpty() && NbtUtil.compoundsOverlap(tag, deniedElements))
			return false;
		return requiredElements.isEmpty() || NbtUtil.isCompoundContained(requiredElements, tag);
	}

	public void addToJson(JsonObject json) {
		if (requiredElements.getSize() > 0)
			json.add("require", NbtUtil.toJson(requiredElements));
		if (deniedElements.getSize() > 0)
			json.add("deny", NbtUtil.toJson(deniedElements));
	}

	public NbtCompound getPreviewTag() {
		return requiredElements;
	}

	public static IngredientEntryCondition fromJson(JsonObject json) {
		IngredientEntryCondition condition = new IngredientEntryCondition();

		boolean flatObject = true;

		if (json.has("require")) {
			if (!json.get("require").isJsonObject())
				throw new JsonParseException("data.require must be an object");
			condition.requiredElements = (NbtCompound) NbtUtil.asTag(json.getAsJsonObject("require"));
			flatObject = false;
		}
		if (json.has("deny")) {
			if (!json.get("deny").isJsonObject())
				throw new JsonParseException("data.deny must be an object");
			condition.deniedElements = (NbtCompound) NbtUtil.asTag(json.getAsJsonObject("deny"));
			flatObject = false;
		}

		if (flatObject) {
			condition.requiredElements = (NbtCompound) NbtUtil.asTag(json);
		}

		return condition;
	}

	public void write(PacketByteBuf buf) {
		buf.writeNbt(requiredElements);
		buf.writeNbt(deniedElements);
	}

	public static IngredientEntryCondition read(PacketByteBuf buf) {
		return new IngredientEntryCondition(buf.readNbt(), buf.readNbt());
	}

}
