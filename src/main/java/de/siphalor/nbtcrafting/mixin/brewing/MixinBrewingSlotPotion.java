/*
 * Copyright 2020 Siphalor
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

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/container/BrewingStandContainer$SlotPotion")
public class MixinBrewingSlotPotion {
	@Inject(method = "matches(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private static void matches(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		/*RecipeManager recipeManager;
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			recipeManager = ClientCore.getRecipeManager();
		} else {
			recipeManager = ((MinecraftServer) FabricLoader.getInstance().getGameInstance()).getRecipeManager();
		}
        if(BrewingRecipe.existsMatchingBase(stack, recipeManager))
        	callbackInfoReturnable.setReturnValue(true);*/
		callbackInfoReturnable.setReturnValue(true);
	}
}
