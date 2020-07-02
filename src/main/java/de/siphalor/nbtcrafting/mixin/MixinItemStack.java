package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.util.duck.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStack implements IItemStack {
	@Shadow
	private CompoundTag tag;

	@Inject(method = "areTagsEqual", at = @At("HEAD"), cancellable = true)
	private static void areTagsEqual(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (!stack1.hasTag() && !stack2.hasTag())
			callbackInfoReturnable.setReturnValue(true);
	}

	@Unique
	@Override
	public void setRawTag(CompoundTag tag) {
		if (tag == null || tag.isEmpty())
			this.tag = null;
		else
			this.tag = tag;
	}
}
