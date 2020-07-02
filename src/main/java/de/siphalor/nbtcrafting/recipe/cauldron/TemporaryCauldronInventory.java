package de.siphalor.nbtcrafting.recipe.cauldron;

import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
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
		world.setBlockState(blockPos, world.getBlockState(blockPos).with(CauldronBlock.LEVEL, level));
	}

	@Override
	public int getInvSize() {
		return 1;
	}

	@Override
	public boolean isInvEmpty() {
		return stackInHand.isEmpty();
	}

	@Override
	public ItemStack getInvStack(int var1) {
		return stackInHand;
	}

	@Override
	public ItemStack takeInvStack(int var1, int var2) {
		return stackInHand.split(var2);
	}

	@Override
	public ItemStack removeInvStack(int var1) {
		playerEntity.setStackInHand(hand, ItemStack.EMPTY);
		ItemStack result = stackInHand;
		stackInHand = ItemStack.EMPTY;
		return result;
	}

	@Override
	public void setInvStack(int var1, ItemStack var2) {
		playerEntity.setStackInHand(hand, var2);
	}

	@Override
	public void markDirty() {
		playerEntity.inventory.markDirty();
	}

	@Override
	public boolean canPlayerUseInv(PlayerEntity var1) {
		return playerEntity == var1;
	}

	@Override
	public void clear() {
		removeInvStack(0);
	}
}
