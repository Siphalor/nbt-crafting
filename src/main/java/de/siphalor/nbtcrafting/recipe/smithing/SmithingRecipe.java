package de.siphalor.nbtcrafting.recipe.smithing;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;

public class SmithingRecipe implements NBTCRecipe<Inventory>, ServerRecipe {

	private final Identifier identifier;

	public final Ingredient base;
	public final Ingredient template;
	public final Ingredient addition;
	public final ItemStack output;
	private final Dollar[] outputDollars;

	public SmithingRecipe(Identifier identifier, Ingredient base, Ingredient template, Ingredient addition, ItemStack output) {
		this.identifier = identifier;
		this.base = base;
		this.template = template;
		this.addition = addition;
		this.output = output;
		this.outputDollars = DollarParser.extractDollars(output.getNbt(), false);
	}


	public Map<String, Object> buildDollarReference(Inventory inv) {
		return ImmutableMap.of(
				"template", NbtUtil.getTagOrEmpty(inv.getStack(0)),
				"base", NbtUtil.getTagOrEmpty(inv.getStack(1)),
				"addition", NbtUtil.getTagOrEmpty(inv.getStack(2))
		);
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		if (!template.test(inventory.getStack(0))) return false;
		if (!base.test(inventory.getStack(1))) return false;
		return addition.test(inventory.getStack(2));
	}

	@Override
	public ItemStack craft(Inventory inventory, DynamicRegistryManager dynamicRegistryManager) {
		return RecipeUtil.applyDollars(output.copy(), outputDollars, buildDollarReference(inventory));
	}

	@Override
	public boolean fits(int i, int j) {
		return false;
	}

	@Override
	public ItemStack getOutput(DynamicRegistryManager dynamicRegistryManager) {
		return output;
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NbtCrafting.SMITHING_TRANSFORM_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return NbtCrafting.SMITHING_TRANSFORM_RECIPE_TYPE;
	}

	public void write(PacketByteBuf packetByteBuf) {
		packetByteBuf.writeIdentifier(identifier);
		template.write(packetByteBuf);
		base.write(packetByteBuf);
		addition.write(packetByteBuf);
		packetByteBuf.writeItemStack(output);
	}

	public static SmithingRecipe from(PacketByteBuf packetByteBuf) {
		Identifier identifier = packetByteBuf.readIdentifier();
		Ingredient template = Ingredient.fromPacket(packetByteBuf);
		Ingredient base = Ingredient.fromPacket(packetByteBuf);
		Ingredient addition = Ingredient.fromPacket(packetByteBuf);
		ItemStack output = packetByteBuf.readItemStack();
		return new SmithingRecipe(identifier, base, template, addition, output);
	}
}
