package de.siphalor.nbtcrafting.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStack {
	@Inject(method = "areTagsEqual", at = @At("HEAD"), cancellable = true)
	private static void areTagsEqual(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(!stack1.hasTag() && !stack2.hasTag())
			callbackInfoReturnable.setReturnValue(true);
	}
}
