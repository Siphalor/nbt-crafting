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

package de.siphalor.nbtcrafting.recipe.cauldron;

import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TemporaryCauldronInventory implements Inventory {
	private ItemStack stackInHand;
	private final PlayerEntity playerEntity;
	private final Hand hand;
	private int level;
	private final World world;
	private final BlockPos blockPos;

	public TemporaryCauldronInventory(PlayerEntity playerEntity, Hand hand, World world, BlockPos blockPos) {
		this.playerEntity = playerEntity;
		this.hand = hand;
		this.world = world;
		this.blockPos = blockPos;
		stackInHand = playerEntity.getStackInHand(hand);
		BlockState blockState = world.getBlockState(blockPos);
		level = blockState.get(CauldronBlock.LEVEL);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
		world.setBlockState(blockPos, world.getBlockState(blockPos).with(CauldronBlock.LEVEL, MathHelper.clamp(level, 0, 3)));
	}

	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int size () {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stackInHand.isEmpty();
	}

	@Override
	public ItemStack getStack(int var1) {
		return stackInHand;
	}

	@Override
	public ItemStack removeStack(int var1, int var2) {
		return stackInHand.split(var2);
	}

	@Override
	public ItemStack removeStack(int var1) {
		playerEntity.setStackInHand(hand, ItemStack.EMPTY);
		ItemStack result = stackInHand;
		stackInHand = ItemStack.EMPTY;
		return result;
	}

	@Override
	public void setStack(int var1, ItemStack var2) {
		playerEntity.setStackInHand(hand, var2);
	}

	@Override
	public void markDirty() {
		playerEntity.inventory.markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity var1) {
		return playerEntity == var1;
	}

	@Override
	public void clear() {
		removeStack(0);
	}
}
