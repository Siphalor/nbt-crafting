package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.NbtCrafting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CraftingResultSlot.class)
public abstract class MixinCraftingResultSlot extends Slot {
	public MixinCraftingResultSlot(Inventory inventory_1, int int_1, int int_2, int int_3) {
		super(inventory_1, int_1, int_2, int_3);
	}

	@Inject(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingInventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onTakeItem(PlayerEntity playerEntity, ItemStack stack, CallbackInfoReturnable<ItemStack> cir, DefaultedList<?> defaultedList, int index, ItemStack old, ItemStack remainder) {
		 if (playerEntity instanceof ServerPlayerEntity && !NbtCrafting.hasClientMod((ServerPlayerEntity) playerEntity)) {
			 ((ServerPlayerEntity) playerEntity).onSlotUpdate(playerEntity.currentScreenHandler, index + 1, remainder);
		 }
	}
}
