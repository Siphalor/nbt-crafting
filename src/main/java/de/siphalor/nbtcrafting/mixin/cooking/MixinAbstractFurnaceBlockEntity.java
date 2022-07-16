package de.siphalor.nbtcrafting.mixin.cooking;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractFurnaceBlockEntity.class)
public class MixinAbstractFurnaceBlockEntity {
	@Inject(
			method = "canAcceptRecipeOutput",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;getCount()I",
					ordinal = 0
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	protected void canAcceptRecipeOutputNBTCheck(@Nullable Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir, ItemStack recipeResult, ItemStack outputStack) {
		if (!ItemStack.areTagsEqual(recipeResult, outputStack)) {
			cir.setReturnValue(false);
		}
	}
}
