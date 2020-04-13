package de.siphalor.nbtcrafting.api;

/**
 * Implementing this on a {@link net.minecraft.recipe.Recipe} will make it not getting synced by {@link net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket}.
 */
public interface ServerRecipe {
}
