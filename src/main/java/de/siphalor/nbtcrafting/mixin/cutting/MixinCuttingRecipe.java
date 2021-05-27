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

package de.siphalor.nbtcrafting.mixin.cutting;

import de.siphalor.nbtcrafting.api.RecipeUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CuttingRecipe.class)
public class MixinCuttingRecipe {
	@Shadow
	@Final
	protected ItemStack output;

	@Shadow
	@Final
	protected Ingredient input;

	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	public void craft(Inventory inventory, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
		ItemStack result = RecipeUtil.getDollarAppliedResult(output, input, inventory);
		if (result != null)
			callbackInfoReturnable.setReturnValue(result);
	}
}
