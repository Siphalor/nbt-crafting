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

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/screen/BrewingStandScreenHandler$IngredientSlot")
public abstract class MixinBrewingSlotIngredient extends Slot {
	public MixinBrewingSlotIngredient(Inventory inventory_1, int int_1, int int_2, int int_3) {
		super(inventory_1, int_1, int_2, int_3);
	}

	@Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		/*RecipeManager recipeManager;
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			recipeManager = ClientCore.getRecipeManager();
		} else {
			recipeManager = ((MinecraftServer) FabricLoader.getInstance().getGameInstance()).getRecipeManager();
		}
        if(BrewingRecipe.existsMatchingIngredient(stack, recipeManager))
        	callbackInfoReturnable.setReturnValue(true);*/
		callbackInfoReturnable.setReturnValue(true);
	}
}
