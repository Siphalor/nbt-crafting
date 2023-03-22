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

package de.siphalor.nbtcrafting.mixin.smithing;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.LegacySmithingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.recipe.IngredientRecipe;

@SuppressWarnings("removal")
@Mixin(LegacySmithingScreenHandler.class)
public abstract class MixinLegacySmithingScreenHandler extends ForgingScreenHandler {
	@Unique
	private static DefaultedList<ItemStack> remainders = null;

	public MixinLegacySmithingScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@Inject(
			method = "updateResult",
			at = @At("HEAD"),
			cancellable = true
	)
	public void onUpdateResult(CallbackInfo callbackInfo) {
		Optional<IngredientRecipe<Inventory>> match = player.world.getRecipeManager().getFirstMatch(NbtCrafting.SMITHING_RECIPE_TYPE, input, player.world);

		if (match.isPresent()) {
			output.setStack(0, match.get().craft(input, player.world.getRegistryManager()));
			callbackInfo.cancel();
		}
	}

	@Inject(
			method = "canTakeOutput",
			at = @At("HEAD"),
			cancellable = true
	)
	protected void canTakeOutput(PlayerEntity playerEntity, boolean stackPresent, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (stackPresent) {
			callbackInfoReturnable.setReturnValue(true);
		}
	}

	@Inject(
			method = "onTakeOutput",
			at = @At("HEAD")
	)
	protected void onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		Optional<IngredientRecipe<Inventory>> match = player.world.getRecipeManager().getFirstMatch(NbtCrafting.SMITHING_RECIPE_TYPE, input, player.world);
		remainders = match.map(inventoryIngredientRecipe -> inventoryIngredientRecipe.getRemainder(input)).orElse(null);
	}

	@Inject(
			method = "onTakeOutput",
			at = @At("TAIL")
	)
	protected void onOutputTaken(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (remainders != null) {
			context.run((world, blockPos) -> {
				RecipeUtil.putRemainders(remainders, input, world, blockPos);
			});
		}
	}
}
