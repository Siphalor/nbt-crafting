package de.siphalor.nbtcrafting.dollar;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import de.siphalor.nbtcrafting.dollar.antlr.DollarLexer;
import de.siphalor.nbtcrafting.dollar.antlr.DollarParser;

public class DollarRuntime {
	public static void compile(String dollar) {
		DollarLexer dollarLexer = new DollarLexer(CharStreams.fromString(dollar));
		DollarParser dollarParser = new DollarParser(new CommonTokenStream(dollarLexer));

	}
	public static void run() {

	}
}
