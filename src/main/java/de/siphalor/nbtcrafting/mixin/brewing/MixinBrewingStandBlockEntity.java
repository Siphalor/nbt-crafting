package de.siphalor.nbtcrafting.mixin.brewing;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.recipe.BrewingRecipe;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity extends LockableContainerBlockEntity {
	@Shadow private DefaultedList<ItemStack> inventory;

	@Shadow public abstract void setInvStack(int slot, ItemStack stack);

	protected MixinBrewingStandBlockEntity(BlockEntityType<?> blockEntityType_1) {
		super(blockEntityType_1);
	}

	@Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
	private void canCraft(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		Optional<BrewingRecipe> recipe = world.getRecipeManager().getFirstMatch(NbtCrafting.BREWING_RECIPE_TYPE, (BrewingStandBlockEntity)(Object) this, world);
		if(recipe.isPresent()) {
			callbackInfoReturnable.setReturnValue(true);
		}
	}

	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	private void craft(CallbackInfo callbackInfo) {
		Optional<BrewingRecipe> recipe = world.getRecipeManager().getFirstMatch(NbtCrafting.BREWING_RECIPE_TYPE, (BrewingStandBlockEntity)(Object) this, world);
		if(recipe.isPresent()) {
			BrewingStandBlockEntity inv = (BrewingStandBlockEntity)(Object) this;
			DefaultedList<ItemStack> remainingStacks = recipe.get().getRemainingStacks(inv);
			ItemStack[] results = recipe.get().craftAll(inv);

			getInvStack(3).decrement(1);
			for (int i = 0; i < 3; i++) {
				if(results[i] != null) {
					setInvStack(i, results[i]);
				}
			}

			RecipeUtil.putRemainders(remainingStacks, inv, world, pos);

			callbackInfo.cancel();
		}
	}

	@Inject(method = "isValidInvStack", at = @At("HEAD"), cancellable = true)
	public void isValidInvStack(int slotId, ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(true);
		// TODO: Fix this again (-> maybe a LoadingCache?)
		/*if(slotId == 3) {
			if(BrewingRecipe.existsMatchingIngredient(stack, world.getRecipeManager()))
				callbackInfoReturnable.setReturnValue(true);
		} else if(slotId != 4) {
			if(BrewingRecipe.existsMatchingBase(stack, world.getRecipeManager()))
				callbackInfoReturnable.setReturnValue(true);
		}*/
	}

}
