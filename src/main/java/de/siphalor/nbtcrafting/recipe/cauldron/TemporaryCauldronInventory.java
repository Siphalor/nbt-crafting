package de.siphalor.nbtcrafting.recipe.cauldron;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.class_5556;
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
			level = blockState.get(class_5556.field_27206);
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
		if (level <= 0) {
			this.level = 0;
			world.setBlockState(blockPos, Blocks.CAULDRON.getDefaultState());
			return;
		}
		this.level = level;
		if (fluid == WATER) {
			world.setBlockState(blockPos, world.getBlockState(blockPos).with(class_5556.field_27206, level));
		}
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
		playerEntity.method_31548().markDirty();
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
