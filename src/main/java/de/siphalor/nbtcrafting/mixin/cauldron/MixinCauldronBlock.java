package de.siphalor.nbtcrafting.mixin.cauldron;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.recipe.cauldron.CauldronRecipe;
import de.siphalor.nbtcrafting.recipe.cauldron.TemporaryCauldronInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
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

import java.util.Optional;

@Mixin(CauldronBlock.class)
public class MixinCauldronBlock {
	@Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
	public void onActivate(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> callbackInfoReturnable) {
		if (!world.isClient()) {
			TemporaryCauldronInventory inventory = new TemporaryCauldronInventory(playerEntity, hand, world, blockPos);
			Optional<CauldronRecipe> cauldronRecipe = world.getRecipeManager().getFirstMatch(NbtCrafting.CAULDRON_RECIPE_TYPE, inventory, world);
			if (cauldronRecipe.isPresent()) {
				DefaultedList<ItemStack> remainingStacks = cauldronRecipe.get().getRemainingStacks(inventory);

				ItemStack itemStack = cauldronRecipe.get().craft(inventory);
				itemStack.onCraft(world, playerEntity, itemStack.getCount());

				if (!playerEntity.method_31548().insertStack(remainingStacks.get(0))) {
					ItemEntity itemEntity = playerEntity.dropItem(remainingStacks.get(0), false);
					if (itemEntity != null) {
						itemEntity.resetPickupDelay();
						itemEntity.setOwner(playerEntity.getUuid());
					}
				}

				if (!playerEntity.method_31548().insertStack(itemStack)) {
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
