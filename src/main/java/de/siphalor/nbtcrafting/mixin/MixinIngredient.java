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

package de.siphalor.nbtcrafting.mixin;

import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.JsonPreprocessor;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.ingredient.*;
import de.siphalor.nbtcrafting.util.duck.ICloneable;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(Ingredient.class)
public abstract class MixinIngredient implements IIngredient, ICloneable {
	@Shadow
	private ItemStack[] matchingStacks;

	@Unique
	private IngredientEntry[] advancedEntries;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onConstruct(@SuppressWarnings("rawtypes") Stream stream, CallbackInfo ci) {
		advancedEntries = null;
	}

	@Inject(method = "cacheMatchingStacks", at = @At("HEAD"), cancellable = true)
	private void createStackArray(CallbackInfo callbackInfo) {
		if (advancedEntries != null) {
			callbackInfo.cancel();
			if (matchingStacks == null || matchingStacks.length == 0) {
				if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
					matchingStacks = Arrays.stream(advancedEntries).flatMap(entry -> entry.getPreviewStacks(true).stream()).distinct().toArray(ItemStack[]::new);
				} else {
					matchingStacks = Arrays.stream(advancedEntries).flatMap(entry -> entry.getPreviewStacks(NbtCrafting.hasClientMod(NbtCrafting.lastServerPlayerEntity.get())).stream()).distinct().toArray(ItemStack[]::new);
				}
				if (matchingStacks.length == 0) {
					matchingStacks = new ItemStack[]{ItemStack.EMPTY};
				}
			}
		}
	}

	@Inject(method = "test", at = @At("HEAD"), cancellable = true)
	public void matches(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (stack == null) {
			callbackInfoReturnable.setReturnValue(false);
			return;
		}
		if (advancedEntries != null) {
			if (advancedEntries.length == 0) {
				callbackInfoReturnable.setReturnValue(stack.isEmpty());
				return;
			}
			for (IngredientEntry advancedEntry : advancedEntries) {
				if (advancedEntry.matches(stack)) {
					callbackInfoReturnable.setReturnValue(true);
					return;
				}
			}
			callbackInfoReturnable.setReturnValue(false);
		}
	}

	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	public void write(PacketByteBuf buf, CallbackInfo callbackInfo) {
		if (NbtCrafting.hasClientMod(NbtCrafting.lastServerPlayerEntity.get())) {
			if (advancedEntries != null) {
				buf.writeBoolean(true);
				buf.writeVarInt(advancedEntries.length);
				for (IngredientEntry entry : advancedEntries) {
					buf.writeBoolean(entry instanceof IngredientMultiStackEntry);
					entry.write(buf);
				}
				callbackInfo.cancel();
			} else {
				buf.writeBoolean(false);
			}
		}
	}

	@Inject(method = "toJson", at = @At("HEAD"), cancellable = true)
	public void toJson(CallbackInfoReturnable<JsonElement> callbackInfoReturnable) {
		if (advancedEntries != null) {
			if (advancedEntries.length == 1) {
				callbackInfoReturnable.setReturnValue(advancedEntries[0].toJson());
				return;
			}
			JsonArray array = new JsonArray();
			for (IngredientEntry advancedEntry : advancedEntries) {
				array.add(advancedEntry.toJson());
			}
			callbackInfoReturnable.setReturnValue(array);
		}
	}

	@Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
	public void isEmpty(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (advancedEntries != null) {
			callbackInfoReturnable.setReturnValue(advancedEntries.length == 0);
		}
	}

	@Unique
	private static Ingredient ofAdvancedEntries(Stream<? extends IngredientEntry> entries) {
		if (entries == null)
			NbtCrafting.logError("Internal error: can't construct ingredient from null entry stream!");
		try {
			Ingredient ingredient;
			//noinspection ConstantConditions
			ingredient = (Ingredient) ((ICloneable) (Object) Ingredient.EMPTY).clone();
			((IIngredient) (Object) ingredient).setAdvancedEntries(entries);
			return ingredient;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return Ingredient.EMPTY;
	}

 	/*
 	 This is client side :/
	@Inject(method = "ofStacks", at = @At("HEAD"), cancellable = true)
	private static void ofStacks(ItemStack[] arr, CallbackInfoReturnable<Ingredient> callbackInfoReturnable) {
		if(Arrays.stream(arr).anyMatch(stack -> stack.hasTag())) {
			callbackInfoReturnable.setReturnValue(ofAdvancedEntries(Arrays.stream(arr).map(stack -> new IngredientStackEntry(stack))));
		}
	}
	*/

	@Inject(method = "fromPacket", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private static void fromPacket(PacketByteBuf buf, CallbackInfoReturnable<Ingredient> callbackInfoReturnable) {
		if (buf.readBoolean()) {
			int length = buf.readVarInt();
			ArrayList<IngredientEntry> entries = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				if (buf.readBoolean())
					entries.add(IngredientMultiStackEntry.read(buf));
				else
					entries.add(IngredientStackEntry.read(buf));
			}
			callbackInfoReturnable.setReturnValue(ofAdvancedEntries(entries.stream()));

		}
	}

	@Inject(method = "fromJson", at = @At("HEAD"), cancellable = true)
	private static void fromJson(JsonElement element, CallbackInfoReturnable<Ingredient> callbackInfoReturnable) {
		if (element == null || element.isJsonNull()) {
			throw new JsonSyntaxException("Item cannot be null");
		}
		if (element.isJsonObject()) {
			if (element.getAsJsonObject().has("data") || element.getAsJsonObject().has("remainder") || element.getAsJsonObject().has("potion"))
				callbackInfoReturnable.setReturnValue(ofAdvancedEntries(Stream.of(advancedEntryFromJson(element.getAsJsonObject()))));
		} else if (element.isJsonArray()) {
			final JsonArray jsonArray = element.getAsJsonArray();

			boolean containsCustomData = false;
			for (JsonElement jsonElement : jsonArray) {
				if (jsonElement.isJsonObject()) {
					JsonObject jsonObject = jsonElement.getAsJsonObject();
					if (jsonObject.has("data") || jsonObject.has("remainder") || jsonObject.has("potion")) {
						containsCustomData = true;
						break;
					}
				}
			}

			if (containsCustomData) {
				if (jsonArray.size() == 0) {
					throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
				}
				callbackInfoReturnable.setReturnValue(ofAdvancedEntries(StreamSupport.stream(jsonArray.spliterator(), false).map(e -> advancedEntryFromJson(JsonHelper.asObject(e, "item")))));
			}
		}
	}

	@Unique
	private static IngredientEntry advancedEntryFromJson(JsonObject jsonObject) {
		if (jsonObject.has("item") && jsonObject.has("tag")) {
			throw new JsonParseException("An ingredient entry is either a tag or an item or a potion, not both");
		}
		if (jsonObject.has("item")) {
			final Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "item"));
			try {
				final Item item = Registry.ITEM.getOrEmpty(identifier).orElseThrow(() -> {
					throw new JsonSyntaxException("Unknown item '" + identifier.toString() + "'");
				});
				IngredientStackEntry entry = new IngredientStackEntry(Registry.ITEM.getRawId(item), loadIngredientEntryCondition(jsonObject));
				if (jsonObject.has("remainder")) {
					entry.setRecipeRemainder(ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "remainder")));
				}
				return entry;
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
		}
		if (jsonObject.has("potion")) {
			final Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "potion"));
			try {
				Registry.POTION.getOrEmpty(identifier).orElseThrow(() -> {
					throw new JsonSyntaxException("Unknown potion '" + identifier.toString() + "'");
				});
				IngredientEntryCondition condition = loadIngredientEntryCondition(jsonObject);
				if (condition.requiredElements == NbtUtil.EMPTY_COMPOUND)
					condition.requiredElements = new NbtCompound();
				condition.requiredElements.putString("Potion", identifier.toString());
				IngredientStackEntry entry = new IngredientStackEntry(Registry.ITEM.getRawId(Items.POTION), condition);
				if (jsonObject.has("remainder")) {
					entry.setRecipeRemainder(ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "remainder")));
				}
				return entry;
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
		}
		if (!jsonObject.has("tag")) {
			throw new JsonParseException("An ingredient entry needs either a tag or an item");
		}
		final Identifier identifier2 = new Identifier(JsonHelper.getString(jsonObject, "tag"));
		final Tag<Item> tag = ItemTags.getTagGroup().getTag(identifier2);
		if (tag == null) {
			throw new JsonSyntaxException("Unknown item tag '" + identifier2 + "'");
		}
		IngredientMultiStackEntry entry = new IngredientMultiStackEntry(tag.values().stream().map(Registry.ITEM::getRawId).collect(Collectors.toList()), loadIngredientEntryCondition(jsonObject));
		entry.setTag(identifier2.toString());
		if (jsonObject.has("remainder")) {
			entry.setRecipeRemainder(ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "remainder")));
		}
		return entry;
	}

	@Unique
	private static IngredientEntryCondition loadIngredientEntryCondition(JsonObject jsonObject) {
		if (jsonObject.has("data")) {
			if (JsonHelper.hasString(jsonObject, "data")) {
				try {
					NbtCompound compoundTag = new StringNbtReader(new StringReader(jsonObject.get("data").getAsString())).parseCompound();
					IngredientEntryCondition condition = new IngredientEntryCondition();
					if (compoundTag.contains("require") || compoundTag.contains("deny")) {
						if (compoundTag.contains("require"))
							condition.requiredElements = compoundTag.getCompound("require");
						if (compoundTag.contains("deny")) condition.deniedElements = compoundTag.getCompound("deny");
					} else {
						condition.requiredElements = compoundTag;
					}
					return condition;
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
				}
			} else if (jsonObject.get("data").isJsonObject()) {
				return IngredientEntryCondition.fromJson((JsonObject) JsonPreprocessor.process(jsonObject.get("data").getAsJsonObject()));
			}
		}
		return new IngredientEntryCondition();
	}

	@Override
	public void setAdvancedEntries(Stream<? extends IngredientEntry> entries) {
		advancedEntries = entries.filter(Objects::nonNull).toArray(IngredientEntry[]::new);
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack, Map<String, Object> reference) {
		if (advancedEntries != null) {
			for (IngredientEntry entry : advancedEntries) {
				if (entry.matches(stack)) {
					ItemStack remainder = entry.getRecipeRemainder(stack, reference);
					if (remainder != null) {
						return remainder;
					}
				}
			}
		}
		return null;
	}
}
