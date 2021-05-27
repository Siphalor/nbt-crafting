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

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.recipe.BrewingRecipe;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity extends LockableContainerBlockEntity {

	protected MixinBrewingStandBlockEntity(BlockEntityType<?> blockEntityType_1) {
		super(blockEntityType_1);
	}

	@Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
	private void canCraft(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		Optional<BrewingRecipe> recipe = world.getRecipeManager().getFirstMatch(NbtCrafting.BREWING_RECIPE_TYPE, (BrewingStandBlockEntity) (Object) this, world);
		if (recipe.isPresent()) {
			callbackInfoReturnable.setReturnValue(true);
		}
	}

	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	private void craft(CallbackInfo callbackInfo) {
		Optional<BrewingRecipe> recipe = world.getRecipeManager().getFirstMatch(NbtCrafting.BREWING_RECIPE_TYPE, (BrewingStandBlockEntity) (Object) this, world);
		if (recipe.isPresent()) {
			BrewingStandBlockEntity inv = (BrewingStandBlockEntity) (Object) this;
			DefaultedList<ItemStack> remainingStacks = recipe.get().getRemainingStacks(inv);
			ItemStack[] results = recipe.get().craftAll(inv);

			getStack(3).decrement(1);
			for (int i = 0; i < 3; i++) {
				if (results[i] != null) {
					setStack(i, results[i]);
				}
			}

			RecipeUtil.putRemainders(remainingStacks, inv, world, pos);

			callbackInfo.cancel();
		}
	}

	@Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
	public void isValidInvStack(int slotId, ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (slotId < 4 && getStack(slotId).isEmpty())
			callbackInfoReturnable.setReturnValue(true);
	}
}
