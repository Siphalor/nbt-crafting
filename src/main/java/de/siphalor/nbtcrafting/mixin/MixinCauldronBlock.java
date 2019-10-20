package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.Core;
import de.siphalor.nbtcrafting.cauldron.CauldronRecipe;
import de.siphalor.nbtcrafting.cauldron.TemporaryCauldronInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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
		if(!world.isClient()) {
			TemporaryCauldronInventory inventory = new TemporaryCauldronInventory(playerEntity, hand, world, blockPos);
			Optional<CauldronRecipe> cauldronRecipe = world.getRecipeManager().getFirstMatch(Core.CAULDRON_RECIPE_TYPE, inventory, world);
			if(cauldronRecipe.isPresent()) {
				ItemStack itemStack = cauldronRecipe.get().craft(inventory);
				if(!playerEntity.inventory.insertStack(itemStack)) {
					ItemEntity itemEntity = playerEntity.dropItem(itemStack, false);
					if(itemEntity != null) {
						itemEntity.resetPickupDelay();
						itemEntity.setOwner(playerEntity.getUuid());
					}
				}
				callbackInfoReturnable.setReturnValue(ActionResult.SUCCESS);
			}
		}
	}
}
