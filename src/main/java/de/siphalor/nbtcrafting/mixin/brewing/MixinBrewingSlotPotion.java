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

package de.siphalor.nbtcrafting.mixin.brewing;

import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.container.Slot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.client.NbtCraftingClient;
import de.siphalor.nbtcrafting.mixin.RecipeManagerAccessor;
import de.siphalor.nbtcrafting.recipe.BrewingRecipe;

@Mixin(targets = "net/minecraft/container/BrewingStandContainer$SlotPotion")
public abstract class MixinBrewingSlotPotion extends Slot {
	@Shadow
	public static boolean matches(ItemStack itemStack) {
		return false;
	}

	public MixinBrewingSlotPotion(Inventory inventory, int invSlot, int xPosition, int yPosition) {
		super(inventory, invSlot, xPosition, yPosition);
	}

	@Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
	public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (callbackInfoReturnable.getReturnValue() || matches(stack)) {
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
				.anyMatch(recipe -> recipe instanceof BrewingRecipe && ((BrewingRecipe) recipe).getBase().test(stack))
		);
	}
}
