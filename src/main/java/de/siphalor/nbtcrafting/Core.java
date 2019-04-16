package de.siphalor.nbtcrafting;

import de.siphalor.nbtcrafting.util.IServerPlayerEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Core implements ModInitializer {
	@SuppressWarnings("WeakerAccess")
	public static final String MODID = "nbtcrafting";
	public static final Identifier PRESENCE_PACKET_ID = new Identifier(MODID, "present");

	private static boolean lastReadNbtPresent = false;
	private static CompoundTag lastReadNbt;
	
	public static RecipeFinder lastRecipeFinder;
	public static ServerPlayerEntity lastServerPlayerEntity;
	
	@SuppressWarnings("unused")
	public static boolean hasLastReadNbt() {
		return lastReadNbtPresent;
	}

	@SuppressWarnings("unused")
	public static void clearLastReadNbt() {
		lastReadNbt = null;
		lastReadNbtPresent = false;
	}
	
	public static void setLastReadNbt(CompoundTag nbt) {
		lastReadNbt = nbt;
		lastReadNbtPresent = true;
	}
	
	public static CompoundTag useLastReadNbt() {
		CompoundTag result = null;
		if(lastReadNbt != null) {
			result = (CompoundTag) lastReadNbt.copy();
			lastReadNbt = null;
		}
		lastReadNbtPresent = false;
		return result;
	}

	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(PRESENCE_PACKET_ID, (packetContext, packetByteBuf) -> {
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) packetContext.getPlayer();
			((IServerPlayerEntity) serverPlayerEntity).nbtCrafting_setClientModPresent(true);
			serverPlayerEntity.networkHandler.sendPacket(new SynchronizeRecipesS2CPacket(serverPlayerEntity.server.getRecipeManager().values()));
			serverPlayerEntity.getRecipeBook().sendInitRecipesPacket(serverPlayerEntity);
		});
	}

	public static boolean hasClientMod(ServerPlayerEntity playerEntity) {
		return ((IServerPlayerEntity) playerEntity).nbtCrafting_hasClientMod();
	}
}
