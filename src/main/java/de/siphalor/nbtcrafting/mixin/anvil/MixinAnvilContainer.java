package de.siphalor.nbtcrafting.mixin.anvil;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.recipe.AnvilRecipe;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AnvilScreenHandler.class)
public abstract class MixinAnvilContainer extends ForgingScreenHandler {

	@Shadow private String newItemName;

	@Shadow @Final private Property levelCost;
	@Unique
	private boolean userChangedName = false;

	public MixinAnvilContainer(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
	public void updateResult(CallbackInfo callbackInfo) {
		Optional<AnvilRecipe> optionalAnvilRecipe = player.world.getRecipeManager().getFirstMatch(NbtCrafting.ANVIL_RECIPE_TYPE, input, player.world);
		if(optionalAnvilRecipe.isPresent()) {
			ItemStack resultStack = optionalAnvilRecipe.get().craft(input);
			if(userChangedName) {
				if (!newItemName.equals(resultStack.getName().getString()))
					resultStack.setCustomName(new LiteralText(newItemName));
				userChangedName = false;
			} else {
				newItemName = resultStack.getName().getString();
				if(player instanceof ServerPlayerEntity) {
					if(NbtCrafting.hasClientMod((ServerPlayerEntity) player)) {
						PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
						packetByteBuf.writeString(newItemName);
						ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, packetByteBuf);
					}
				}
			}

			input.setInvStack(0, resultStack);
			resultStack.onCraft(player.world, player, resultStack.getCount());

			levelCost.set(optionalAnvilRecipe.get().getLevels());
			sendContentUpdates();

			callbackInfo.cancel();
		}
	}

	@Inject(method = "setNewItemName", at = @At("HEAD"))
	public void onNewItemNameSet(String newNewItemName, CallbackInfo callbackInfo) {
		userChangedName = true;
	}
}
