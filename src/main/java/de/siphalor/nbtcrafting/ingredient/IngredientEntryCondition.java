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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Pair;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.nbt.NbtException;
import de.siphalor.nbtcrafting.api.nbt.NbtIterator;
import de.siphalor.nbtcrafting.api.nbt.NbtNumberRange;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;

public class IngredientEntryCondition {
	public static final IngredientEntryCondition EMPTY = new IngredientEntryCondition(NbtUtil.EMPTY_COMPOUND, NbtUtil.EMPTY_COMPOUND);

	public NbtCompound requiredElements;
	public NbtCompound deniedElements;
	private NbtCompound previewTag;

	public IngredientEntryCondition() {
		requiredElements = NbtUtil.EMPTY_COMPOUND;
		deniedElements = NbtUtil.EMPTY_COMPOUND;
	}

	public IngredientEntryCondition(NbtCompound requiredElements, NbtCompound deniedElements) {
		this.requiredElements = requiredElements;
		this.deniedElements = deniedElements;
	}

	public boolean matches(ItemStack stack) {
		if (!stack.hasNbt()) {
			return requiredElements.isEmpty();
		}
		NbtCompound tag = stack.getNbt();
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
		if (previewTag == null) {
			previewTag = requiredElements.copy();
			List<Pair<String[], NbtElement>> dollarRangeKeys = new ArrayList<>();
			NbtIterator.iterateTags(previewTag, (path, key, tag) -> {
				if (NbtUtil.isString(tag)) {
					String text = NbtUtil.asString(tag);
					if (text.startsWith("$")) {
						dollarRangeKeys.add(new Pair<>(NbtUtil.splitPath(path + key), NbtNumberRange.ofString(text.substring(1)).getExample()));
					}
				}
				return false;
			});
			for (Pair<String[], NbtElement> dollarRangeKey : dollarRangeKeys) {
				try {
					NbtUtil.put(previewTag, dollarRangeKey.getLeft(), dollarRangeKey.getRight());
				} catch (NbtException e) {
					NbtCrafting.logWarn("Failed to set dollar range value " + dollarRangeKey.getRight() + " for key " + String.join(".", dollarRangeKey.getLeft()) + " in preview tag " + previewTag);
				}
			}
		}
		return previewTag;
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
		if (json.has("potion")) {
			Identifier potion = new Identifier(JsonHelper.getString(json, "potion"));
			if (Registries.POTION.getOrEmpty(potion).isPresent()) {
				if (condition.requiredElements == NbtUtil.EMPTY_COMPOUND) {
					condition.requiredElements = new NbtCompound();
				}
				condition.requiredElements.putString("Potion", potion.toString());
			} else {
				new JsonSyntaxException("Unknown potion '" + potion + "'").printStackTrace();
			}
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
