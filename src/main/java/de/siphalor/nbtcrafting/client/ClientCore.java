package de.siphalor.nbtcrafting.client;

import de.siphalor.nbtcrafting.Core;
import de.siphalor.nbtcrafting.client.mixin.AnvilScreenAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.util.PacketByteBuf;

public class ClientCore implements ClientModInitializer {
	public static boolean sentModPresent = false;

	public static void sendModPresent() {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		ClientSidePacketRegistry.INSTANCE.sendToServer(Core.PRESENCE_PACKET_ID, buffer);
		sentModPresent = true;
	}

	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(Core.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, (packetContext, packetByteBuf) -> {
			if(MinecraftClient.getInstance().currentScreen instanceof AnvilScreen) {
				((AnvilScreenAccessor) MinecraftClient.getInstance().currentScreen).getNameField().setText(packetByteBuf.readString());
			} else
				packetByteBuf.readString();
		});
	}
}
