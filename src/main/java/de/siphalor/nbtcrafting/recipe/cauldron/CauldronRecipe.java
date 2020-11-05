package de.siphalor.nbtcrafting.recipe.cauldron;

import com.google.common.collect.ImmutableMap;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Map;

public class CauldronRecipe implements NBTCRecipe<TemporaryCauldronInventory>, ServerRecipe {
	private final Identifier identifier;
	public final Ingredient input;
	public final ItemStack output;
	public final Identifier fluid;
	public final int levels;
	private final Dollar[] outputDollars;

	public CauldronRecipe(Identifier id, Ingredient ingredient, ItemStack output, Identifier fluid, int levels) {
		this.identifier = id;
		this.input = ingredient;
		this.output = output;
		this.fluid = fluid;
		this.levels = levels;
		this.outputDollars = DollarParser.extractDollars(output.getTag(), false);
	}

	public void write(PacketByteBuf packetByteBuf) {
		packetByteBuf.writeIdentifier(identifier);
		input.write(packetByteBuf);
		packetByteBuf.writeItemStack(output);
		packetByteBuf.writeIdentifier(fluid);
		packetByteBuf.writeShort(levels);
	}

	public static CauldronRecipe from(PacketByteBuf packetByteBuf) {
		Identifier identifier = packetByteBuf.readIdentifier();
		Ingredient input = Ingredient.fromPacket(packetByteBuf);
		ItemStack output = packetByteBuf.readItemStack();
		Identifier fluid = packetByteBuf.readIdentifier();
		int levels = packetByteBuf.readShort();
		return new CauldronRecipe(identifier, input, output, fluid, levels);
	}

	@Override
	public boolean matches(TemporaryCauldronInventory inventory, World world) {
		return (fluid == null || fluid.equals(inventory.getFluid())) && inventory.getLevel() >= levels && input.test(inventory.getStack(0));
	}

	@Override
	public ItemStack craft(TemporaryCauldronInventory inventory) {
		inventory.setLevel(inventory.getLevel() - levels);

		inventory.getStack(0).decrement(1);

		return RecipeUtil.applyDollars(output.copy(), outputDollars, buildDollarReference(inventory));
	}

	@Override
	public boolean fits(int i, int i1) {
		return false;
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public DefaultedList<Ingredient> getPreviewInputs() {
		return DefaultedList.copyOf(Ingredient.EMPTY, input);
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NbtCrafting.CAULDRON_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return NbtCrafting.CAULDRON_RECIPE_TYPE;
	}

	@Override
	public Map<String, Object> buildDollarReference(TemporaryCauldronInventory inv) {
		return ImmutableMap.of("ingredient", NbtUtil.getTagOrEmpty(inv.getStack(0)));
	}
}
