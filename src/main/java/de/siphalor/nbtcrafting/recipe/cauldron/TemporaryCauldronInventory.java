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

package de.siphalor.nbtcrafting.recipe.cauldron;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WaterCauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TemporaryCauldronInventory implements Inventory {
	public static final Identifier AIR = new Identifier("air");
	public static final Identifier WATER = new Identifier("water");
	public static final Identifier LAVA = new Identifier("lava");
	public static final Identifier POWDER_SNOW = new Identifier("powder_snow");

	private ItemStack stackInHand;
	private final PlayerEntity playerEntity;
	private final Hand hand;
	private final Identifier fluid;
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
		if (blockState.getBlock() == Blocks.WATER_CAULDRON) {
			fluid = WATER;
			level = blockState.get(WaterCauldronBlock.LEVEL);
		} else if (blockState.getBlock() == Blocks.POWDER_SNOW_CAULDRON) {
			fluid = POWDER_SNOW;
			level = blockState.get(WaterCauldronBlock.LEVEL);
		} else if (blockState.getBlock() == Blocks.LAVA_CAULDRON) {
			fluid = LAVA;
			level = 3;
		} else if (blockState.getBlock() == Blocks.CAULDRON) {
			fluid = AIR;
			level = 0;
		} else {
			fluid = null;
			level = 0;
		}
	}

	public Identifier getFluid() {
		return fluid;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		if (level <= 0) { // The cauldron needs to be cleared
			this.level = 0;
			world.setBlockState(blockPos, Blocks.CAULDRON.getDefaultState());
			return;
		}
		if (fluid == WATER || fluid == POWDER_SNOW) {
			if (this.level <= 0) { // It was an empty cauldron before so we're replacing it
				Block block = fluid == WATER ? Blocks.WATER_CAULDRON : Blocks.POWDER_SNOW_CAULDRON;
				world.setBlockState(blockPos, block.getDefaultState().with(WaterCauldronBlock.LEVEL, Math.min(level, 3)));
			} else { // There was a water cauldron before so we're reusing it (mod compat.)
				world.setBlockState(blockPos, world.getBlockState(blockPos).with(WaterCauldronBlock.LEVEL, Math.min(level, 3)));
			}
		} else if (fluid == LAVA && this.level <= 0) { // There was an empty cauldron before so we're replacing it
			world.setBlockState(blockPos, Blocks.LAVA_CAULDRON.getDefaultState());
		}
		this.level = level;
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
		playerEntity.getInventory().markDirty();
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
