package de.siphalor.nbtcrafting.client;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.mixin.client.AnvilScreenAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.network.PacketByteBuf;

public class NbtCraftingClient implements ClientModInitializer {
	public static boolean sentModPresent = false;

	public static void sendModPresent() {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		ClientSidePacketRegistry.INSTANCE.sendToServer(NbtCrafting.PRESENCE_PACKET_ID, buffer);
		sentModPresent = true;
	}

	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(NbtCrafting.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, (packetContext, packetByteBuf) -> {
			if(MinecraftClient.getInstance().currentScreen instanceof AnvilScreen) {
				((AnvilScreenAccessor) MinecraftClient.getInstance().currentScreen).getNameField().setText(packetByteBuf.readString());
			} else
				packetByteBuf.readString();
		});
	}
}
