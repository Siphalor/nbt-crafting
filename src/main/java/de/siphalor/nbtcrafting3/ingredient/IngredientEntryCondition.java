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

package de.siphalor.nbtcrafting3.ingredient;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.JsonHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Pair;

import de.siphalor.nbtcrafting3.NbtCrafting;
import de.siphalor.nbtcrafting3.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting3.dollar.DollarExtractor;
import de.siphalor.nbtcrafting3.dollar.DollarUtil;
import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.exception.UnresolvedDollarReferenceException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;

public class IngredientEntryCondition {
	public static final IngredientEntryCondition EMPTY = new IngredientEntryCondition(NbtUtil.EMPTY_COMPOUND, NbtUtil.EMPTY_COMPOUND);

	public CompoundTag requiredElements;
	public CompoundTag deniedElements;
	public List<Pair<String, DollarPart>> dollarPredicates;

	protected IngredientEntryCondition() {
		requiredElements = NbtUtil.EMPTY_COMPOUND;
		deniedElements = NbtUtil.EMPTY_COMPOUND;
		dollarPredicates = null;
	}

	public IngredientEntryCondition(CompoundTag requiredElements, CompoundTag deniedElements) {
		this.requiredElements = requiredElements;
		this.deniedElements = deniedElements;
	}

	public boolean matches(ItemStack stack) {
		if (!stack.hasTag()) {
			return requiredElements.isEmpty();
		}
		CompoundTag tag = stack.getTag();
		//noinspection ConstantConditions
		if (!deniedElements.isEmpty() && NbtUtil.compoundsOverlap(tag, deniedElements))
			return false;
		if (!requiredElements.isEmpty() && !NbtUtil.isCompoundContained(requiredElements, tag))
			return false;
		if (dollarPredicates != null && !dollarPredicates.isEmpty()) {
			for (Pair<String, DollarPart> predicate : dollarPredicates) {
				try {
					if (!DollarUtil.asBoolean(predicate.getRight().evaluate(ref -> {
						if ("$".equals(ref)) {
							return tag;
						}
						throw new UnresolvedDollarReferenceException(ref);
					}))) {
						return false;
					}
				} catch (DollarEvaluationException e) {
					NbtCrafting.logWarn("Failed to evaluate dollar predicate (" + predicate.getLeft() + "): " + e.getMessage());
				}
			}
		}
		return true;
	}

	public void addToJson(JsonObject json) {
		if (requiredElements.getSize() > 0)
			json.add("require", NbtUtil.toJson(requiredElements));
		if (deniedElements.getSize() > 0)
			json.add("deny", NbtUtil.toJson(deniedElements));
		if (dollarPredicates != null && !dollarPredicates.isEmpty()) {
			JsonArray array = new JsonArray();
			for (Pair<String, DollarPart> condition : dollarPredicates) {
				array.add(condition.getLeft());
			}
			json.add("conditions", array);
		}
	}

	public CompoundTag getPreviewTag() {
		return requiredElements;
	}

	public static IngredientEntryCondition fromJson(JsonObject json) {
		IngredientEntryCondition condition = new IngredientEntryCondition();

		boolean flatObject = true;

		if (json.has("require")) {
			if (!json.get("require").isJsonObject())
				throw new JsonParseException("data.require must be an object");
			condition.requiredElements = (CompoundTag) NbtUtil.asTag(json.getAsJsonObject("require"));
			flatObject = false;
		}
		if (json.has("deny")) {
			if (!json.get("deny").isJsonObject())
				throw new JsonParseException("data.deny must be an object");
			condition.deniedElements = (CompoundTag) NbtUtil.asTag(json.getAsJsonObject("deny"));
			flatObject = false;
		}
		if (json.has("conditions")) {
			if (!json.get("conditions").isJsonArray())
				throw new JsonParseException("data.conditions must be an array");
			JsonArray array = json.getAsJsonArray("conditions");
			List<Pair<String, DollarPart>> predicates = new ArrayList<>(array.size());
			for (JsonElement jsonElement : array) {
				if (!JsonHelper.isString(jsonElement))
					throw new JsonParseException("data.conditions must be an array of strings");
				predicates.add(new Pair<>(jsonElement.getAsString(), DollarExtractor.parse(jsonElement.getAsString())));
			}
			condition.dollarPredicates = predicates;
			flatObject = false;
		}

		if (flatObject) {
			condition.requiredElements = (CompoundTag) NbtUtil.asTag(json);
		}

		return condition;
	}

	public void write(PacketByteBuf buf) {
		buf.writeCompoundTag(requiredElements);
		buf.writeCompoundTag(deniedElements);
	}

	public static IngredientEntryCondition read(PacketByteBuf buf) {
		return new IngredientEntryCondition(buf.readCompoundTag(), buf.readCompoundTag());
	}
}
