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

package de.siphalor.nbtcrafting.mixin.cooking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import de.siphalor.nbtcrafting.util.duck.IItemStack;

@Mixin(CookingRecipeSerializer.class)
public abstract class MixinCookingRecipeSerializer {
	@Unique
	private NbtCompound resultTag = null;

	@Redirect(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/AbstractCookingRecipe;", at = @At(value = "INVOKE", target = "net/minecraft/util/JsonHelper.getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;", ordinal = 0))
	public String getItemIdentifier(JsonObject jsonObject, String resultPropertyName) {
		resultTag = null;
		if (!jsonObject.has(resultPropertyName) || !jsonObject.get(resultPropertyName).isJsonObject()) {
			return JsonHelper.getString(jsonObject, resultPropertyName);
		}
		ItemStack output = ShapedRecipe.outputFromJson(jsonObject.getAsJsonObject(resultPropertyName));
		resultTag = output.getNbt();
		return Registries.ITEM.getId(output.getItem()).toString();
	}

	@Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/AbstractCookingRecipe;", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void onRecipeReady(Identifier identifier, JsonObject jsonObject, CallbackInfoReturnable<AbstractCookingRecipe> callbackInfoReturnable, String group, CookingRecipeCategory category, JsonElement ingredientJson, Ingredient ingredient, String itemId, Identifier itemIdentifier, ItemStack stack, float experience, int cookingTime) {
		//noinspection ConstantConditions
		((IItemStack) (Object) stack).nbtCrafting$setRawTag(resultTag);
	}
}
