package de.siphalor.nbtcrafting.mixin.anvil;

import de.siphalor.nbtcrafting.NbtCrafting;
import net.minecraft.client.network.packet.ContainerSlotUpdateS2CPacket;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/container/AnvilContainer$2")
public abstract class MixinAnvilContainerResultSlot extends Slot {
	@Shadow(aliases = "field_7781")
	private AnvilContainer container;

	@Unique
	private ItemStack originalBaseStack;

	public MixinAnvilContainerResultSlot(Inventory inventory, int invSlot, int xPosition, int yPosition) {
		super(inventory, invSlot, xPosition, yPosition);
	}

	@Inject(
			method = "canTakeItems(Lnet/minecraft/entity/player/PlayerEntity;)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	public void canTakeItemsTop(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (((AnvilContainerAccessor) container).getLevelCost().get() <= 0) {
			ItemStack base = container.getSlot(0).getStack();
			if (!ItemStack.areItemsEqual(getStack(), base) || !ItemStack.areTagsEqual(getStack(), base)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(
			method = "onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)" +
					"Lnet/minecraft/item/ItemStack;",
			at = @At("HEAD")
	)
	public void onTakeItemTop(CallbackInfoReturnable<ItemStack> cir) {
		originalBaseStack = container.getSlot(0).getStack();
	}
	
	@Inject(
			method = "onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)" +
					"Lnet/minecraft/item/ItemStack;",
			at = @At("RETURN")
	)
	public void onItemTaken(PlayerEntity player, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
		if (originalBaseStack != null) {
			originalBaseStack.decrement(1);
			container.setStackInSlot(0, originalBaseStack);
		}
		if (player instanceof ServerPlayerEntity) {
			if (!NbtCrafting.hasClientMod((ServerPlayerEntity) player)) {
				((ServerPlayerEntity) player).networkHandler.sendPacket(new ContainerSlotUpdateS2CPacket(
						-1, 0, stack
				));
			}
		}
	}
}
