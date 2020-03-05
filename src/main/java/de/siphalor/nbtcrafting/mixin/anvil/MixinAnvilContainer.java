package de.siphalor.nbtcrafting.mixin.anvil;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.anvil.AnvilRecipe;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.class_4861;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.BlockContext;
import net.minecraft.container.ContainerType;
import net.minecraft.container.Property;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AnvilContainer.class)
public abstract class MixinAnvilContainer extends class_4861 {
	@Shadow @Final private Property levelCost;

	@Shadow private String newItemName;

	@Unique
	private boolean userChangedName = false;

	public MixinAnvilContainer(ContainerType<?> containerType, int i, PlayerInventory playerInventory, BlockContext blockContext) {
		super(containerType, i, playerInventory, blockContext);
	}

	@Inject(method = "method_24928", at = @At("HEAD"), cancellable = true)
	public void updateResult(CallbackInfo callbackInfo) {
		Optional<AnvilRecipe> optionalAnvilRecipe = field_22482.world.getRecipeManager().getFirstMatch(NbtCrafting.ANVIL_RECIPE_TYPE, field_22480, field_22482.world);
		if(optionalAnvilRecipe.isPresent()) {
			ItemStack resultStack = optionalAnvilRecipe.get().craft(field_22480);
			if(userChangedName) {
				if (!newItemName.equals(resultStack.getName().getString()))
					resultStack.setCustomName(new LiteralText(newItemName));
				userChangedName = false;
			} else {
				newItemName = resultStack.getName().getString();
				if(field_22482 instanceof ServerPlayerEntity) {
					if(NbtCrafting.hasClientMod((ServerPlayerEntity) field_22482)) {
						PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
						packetByteBuf.writeString(newItemName);
						ServerSidePacketRegistry.INSTANCE.sendToPlayer(field_22482, NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, packetByteBuf);
					}
				}
			}

			field_22479.setInvStack(0, resultStack);
			levelCost.set(optionalAnvilRecipe.get().levels);
			sendContentUpdates();

			callbackInfo.cancel();
		}
	}

	@Inject(method = "setNewItemName", at = @At("HEAD"))
	public void onNewItemNameSet(String newNewItemName, CallbackInfo callbackInfo) {
		userChangedName = true;
	}
}
