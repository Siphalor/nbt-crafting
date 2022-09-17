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

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.nbtcrafting3.NbtCrafting;
import de.siphalor.nbtcrafting3.api.RecipeUtil;
import de.siphalor.nbtcrafting3.recipe.BrewingRecipe;

@SuppressWarnings("ConstantConditions")
@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity extends LockableContainerBlockEntity {
	@Unique
	private static World lastWorld;
	@Unique
	private static BrewingStandBlockEntity lastBlockEntity;
	@Unique
	private static BlockPos lastBlockPos;

	protected MixinBrewingStandBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;canCraft(Lnet/minecraft/util/collection/DefaultedList;)Z"))
	private static void beforeCanCraft(World world, BlockPos blockPos, BlockState blockState, BrewingStandBlockEntity blockEntity, CallbackInfo callbackInfo) {
		lastWorld = world;
		lastBlockEntity = blockEntity;
		lastBlockPos = blockPos;
	}

	@Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
	private static void canCraft(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		Optional<BrewingRecipe> recipe = lastWorld.getRecipeManager().getFirstMatch(NbtCrafting.BREWING_RECIPE_TYPE, lastBlockEntity, lastWorld);
		if (recipe.isPresent()) {
			callbackInfoReturnable.setReturnValue(true);
		}
	}

	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	private static void craft(World world, BlockPos pos, DefaultedList<ItemStack> invList, CallbackInfo callbackInfo) {
		Optional<BrewingRecipe> recipe = lastWorld.getRecipeManager().getFirstMatch(NbtCrafting.BREWING_RECIPE_TYPE, lastBlockEntity, lastWorld);
		if (recipe.isPresent()) {
			DefaultedList<ItemStack> remainingStacks = recipe.get().getRemainder(lastBlockEntity);
			ItemStack[] results = recipe.get().craftAll(lastBlockEntity);

			lastBlockEntity.getStack(3).decrement(1);
			for (int i = 0; i < 3; i++) {
				if (results[i] != null) {
					lastBlockEntity.setStack(i, results[i]);
				}
			}

			RecipeUtil.putRemainders(remainingStacks, lastBlockEntity, lastWorld, lastBlockPos);

			world.syncWorldEvent(WorldEvents.BREWING_STAND_BREWS, pos, 0);

			callbackInfo.cancel();
		}
	}

	@Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
	public void isValidInvStack(int slotId, ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (slotId < 4 && getStack(slotId).isEmpty()) {
			if (stack.getItem() != Items.BLAZE_POWDER) {
				callbackInfoReturnable.setReturnValue(true);
			}
		}
	}
}
