package de.siphalor.nbtcrafting.client;

import de.siphalor.nbtcrafting.Core;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.PacketByteBuf;

public class ClientCore {
	public static boolean sentModPresent = false;

	public static void sendModPresent() {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		ClientSidePacketRegistry.INSTANCE.sendToServer(Core.PRESENCE_PACKET_ID, buffer);
		sentModPresent = true;
	}

	public static RecipeManager getRecipeManager() {
        return MinecraftClient.getInstance().getServer().getRecipeManager();
	}
}
