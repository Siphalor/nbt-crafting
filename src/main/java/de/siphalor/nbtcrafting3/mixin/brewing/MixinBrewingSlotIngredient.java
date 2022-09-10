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

package de.siphalor.nbtcrafting3.mixin.brewing;

import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.nbtcrafting3.NbtCrafting;
import de.siphalor.nbtcrafting3.client.NbtCraftingClient;
import de.siphalor.nbtcrafting3.mixin.RecipeManagerAccessor;
import de.siphalor.nbtcrafting3.recipe.BrewingRecipe;

@Mixin(targets = "net/minecraft/screen/BrewingStandScreenHandler$IngredientSlot")
public abstract class MixinBrewingSlotIngredient extends Slot {
	public MixinBrewingSlotIngredient(Inventory inventory_1, int int_1, int int_2, int int_3) {
		super(inventory_1, int_1, int_2, int_3);
	}

	@Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
	public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (callbackInfoReturnable.getReturnValue() || BrewingRecipeRegistry.isValidIngredient(stack)) {
			callbackInfoReturnable.setReturnValue(true);
			return;
		}
		RecipeManagerAccessor recipeManager;
		if (inventory instanceof BrewingStandBlockEntity) {
			recipeManager = (RecipeManagerAccessor) ((BrewingStandBlockEntity) inventory).getWorld().getRecipeManager();
		} else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			recipeManager = (RecipeManagerAccessor) NbtCraftingClient.getClientRecipeManager();
		} else {
			NbtCrafting.logError("Failed to get recipe manager in brewing stand container class!");
			return;
		}
		Map<Identifier, Recipe<Inventory>> recipes = recipeManager.callGetAllOfType(NbtCrafting.BREWING_RECIPE_TYPE);
		callbackInfoReturnable.setReturnValue(recipes.values().stream()
				.anyMatch(recipe -> recipe instanceof BrewingRecipe && ((BrewingRecipe) recipe).getIngredient().test(stack))
		);
	}
}
