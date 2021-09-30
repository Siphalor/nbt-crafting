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

package de.siphalor.nbtcrafting.mixin.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.JsonPreprocessor;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.util.duck.IItemStack;

@Mixin(ShapedRecipe.class)
public abstract class MixinShapedRecipe {
	@Shadow
	@Final
	private ItemStack output;

	@Shadow
	@Final
	private DefaultedList<Ingredient> input;

	@Inject(method = "outputFromJson", at = @At("HEAD"))
	private static void handlePotions(JsonObject json, CallbackInfoReturnable<ItemStack> ci) {
		if (json.has("potion")) {
			Identifier identifier = new Identifier(JsonHelper.getString(json, "potion"));
			if (!Registry.POTION.getOrEmpty(identifier).isPresent())
				throw new JsonParseException("The given resulting potion does not exist!");
			JsonObject dataObject;
			if (!json.has("data")) {
				dataObject = new JsonObject();
				json.add("data", dataObject);
			} else
				dataObject = JsonHelper.getObject(json, "data");
			dataObject.addProperty("Potion", identifier.toString());
			json.addProperty("item", "minecraft:potion");
		}
	}

	@Inject(
			method = "outputFromJson",
			at = @At(value = "INVOKE", target = "com/google/gson/JsonObject.has(Ljava/lang/String;)Z", remap = false)
	)
	private static void deserializeItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> ci) {
		NbtCrafting.clearLastReadNbt();
		if (json.has("data")) {
			if (JsonHelper.hasString(json, "data")) {
				try {
					NbtCrafting.setLastReadNbt(new StringNbtReader(new StringReader(json.get("data").getAsString())).parseCompound());
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
				}
			} else {
				NbtCrafting.setLastReadNbt((NbtCompound) NbtUtil.asTag(JsonPreprocessor.process(JsonHelper.getObject(json, "data"))));
			}
			json.remove("data");
		}
	}

	@Inject(
			method = "outputFromJson", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void constructDeserializedItemStack(JsonObject json, CallbackInfoReturnable<ItemStack> ci, Item item, int amount) {
		ItemStack stack = new ItemStack(item, amount);
		if (NbtCrafting.hasLastReadNbt()) {
			NbtCompound lastReadNbt = NbtCrafting.useLastReadNbt();

			//noinspection ConstantConditions
			((IItemStack) (Object) stack).nbtCrafting$setRawTag(lastReadNbt);
		}
		ci.setReturnValue(stack);
	}

	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	public void craft(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
		ItemStack result = RecipeUtil.getDollarAppliedResult(output, input, craftingInventory);
		if (result != null) callbackInfoReturnable.setReturnValue(result);
	}
}
