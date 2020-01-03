package de.siphalor.nbtcrafting.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InputSlotFiller.class)
public abstract class MixinInputSlotFiller {
	@Shadow
	protected PlayerInventory inventory;

	@Redirect(method = "fillInputSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;method_7371(Lnet/minecraft/item/ItemStack;)I"))
	private int playerInventoryFindStack(PlayerInventory inventory, ItemStack stack) {
		for(int i = 0; i < inventory.main.size(); i++) {
			ItemStack stack2 = inventory.main.get(i);
			if(stack.getItem() == stack2.getItem() && ItemStack.areTagsEqual(stack, stack2))
				return i;
		}
		if(!stack.hasTag()) {
			for(int i = 0; i < inventory.main.size(); i++) {
				ItemStack stack2 = inventory.main.get(i);
				if(stack2.hasTag() && stack.isItemEqualIgnoreDamage(stack2))
					return i;
			}
		}
		return -1;
	}
}
