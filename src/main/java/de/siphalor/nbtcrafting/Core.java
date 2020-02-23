package de.siphalor.nbtcrafting;

import de.siphalor.nbtcrafting.anvil.AnvilRecipe;
import de.siphalor.nbtcrafting.anvil.AnvilRecipeSerializer;
import de.siphalor.nbtcrafting.api.RecipeTypeHelper;
import de.siphalor.nbtcrafting.brewing.BrewingRecipe;
import de.siphalor.nbtcrafting.brewing.BrewingRecipeSerializer;
import de.siphalor.nbtcrafting.cauldron.CauldronRecipe;
import de.siphalor.nbtcrafting.cauldron.CauldronRecipeSerializer;
import de.siphalor.nbtcrafting.util.IServerPlayerEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Core implements ModInitializer {
	public static final String MODID = "nbtcrafting";
	public static final Identifier PRESENCE_PACKET_ID = new Identifier(MODID, "present");
	public static final Identifier UPDATE_ANVIL_TEXT_S2C_PACKET_ID = new Identifier(MODID, "update_anvil_text");

	public static final RecipeType<BrewingRecipe> BREWING_RECIPE_TYPE = registerRecipeType("brewing");
	public static final BrewingRecipeSerializer BREWING_RECIPE_SERIALIZER = registerRecipeSerializer("brewing", new BrewingRecipeSerializer());

	public static final RecipeType<CauldronRecipe> CAULDRON_RECIPE_TYPE = registerRecipeType("cauldron");
	public static final CauldronRecipeSerializer CAULDRON_RECIPE_SERIALIZER = registerRecipeSerializer("cauldron", new CauldronRecipeSerializer());

	public static final RecipeType<AnvilRecipe> ANVIL_RECIPE_TYPE = registerRecipeType("anvil");
	public static final AnvilRecipeSerializer ANVIL_RECIPE_SERIALIZER = registerRecipeSerializer("anvil", new AnvilRecipeSerializer());

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
			result = lastReadNbt.copy();
			lastReadNbt = null;
		}
		lastReadNbtPresent = false;
		return result;
	}

	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(PRESENCE_PACKET_ID, (packetContext, packetByteBuf) -> {
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) packetContext.getPlayer();
			((IServerPlayerEntity) serverPlayerEntity).setClientModPresent(true);
			serverPlayerEntity.networkHandler.sendPacket(new SynchronizeRecipesS2CPacket(serverPlayerEntity.server.getRecipeManager().values()));
			serverPlayerEntity.getRecipeBook().sendInitRecipesPacket(serverPlayerEntity);
		});
	}

	public static boolean hasClientMod(ServerPlayerEntity playerEntity) {
		if(!(playerEntity instanceof IServerPlayerEntity))
			return false;
		return ((IServerPlayerEntity) playerEntity).hasClientMod();
	}

	public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String name) {
		Identifier recipeTypeId = new Identifier(MODID, name);
		RecipeTypeHelper.addToSyncBlacklist(recipeTypeId);
		return Registry.register(Registry.RECIPE_TYPE, recipeTypeId, new RecipeType<T>() {
			@Override
			public String toString() {
				return name;
			}
		});
	}

	public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerRecipeSerializer(String name, S recipeSerializer) {
		return Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MODID, name), recipeSerializer);
	}
}
