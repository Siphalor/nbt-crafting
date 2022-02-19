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

package de.siphalor.nbtcrafting.mixin.cauldron;

import java.util.Optional;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.recipe.cauldron.CauldronRecipe;
import de.siphalor.nbtcrafting.recipe.cauldron.TemporaryCauldronInventory;

@Mixin(AbstractCauldronBlock.class)
public class MixinCauldronBlock {
	@Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
	public void onActivate(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> callbackInfoReturnable) {
		if (!world.isClient()) {
			TemporaryCauldronInventory inventory = new TemporaryCauldronInventory(playerEntity, hand, world, blockPos);
			Optional<CauldronRecipe> cauldronRecipe = world.getRecipeManager().getFirstMatch(NbtCrafting.CAULDRON_RECIPE_TYPE, inventory, world);
			if (cauldronRecipe.isPresent()) {
				DefaultedList<ItemStack> remainingStacks = cauldronRecipe.get().getRemainder(inventory);

				ItemStack itemStack = cauldronRecipe.get().craft(inventory);
				itemStack.onCraft(world, playerEntity, itemStack.getCount());

				if (!playerEntity.getInventory().insertStack(remainingStacks.get(0))) {
					ItemEntity itemEntity = playerEntity.dropItem(remainingStacks.get(0), false);
					if (itemEntity != null) {
						itemEntity.resetPickupDelay();
						itemEntity.setOwner(playerEntity.getUuid());
					}
				}

				if (!playerEntity.getInventory().insertStack(itemStack)) {
					ItemEntity itemEntity = playerEntity.dropItem(itemStack, false);
					if (itemEntity != null) {
						itemEntity.resetPickupDelay();
						itemEntity.setOwner(playerEntity.getUuid());
					}
				}
				callbackInfoReturnable.setReturnValue(ActionResult.SUCCESS);
			}
		}
	}
}
