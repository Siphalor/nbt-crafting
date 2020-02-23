package de.siphalor.nbtcrafting.api;

/**
 * Implementing this on a {@link net.minecraft.recipe.Recipe} will make it not getting synced by {@link net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket}.
 */
public interface ServerRecipe {
}
