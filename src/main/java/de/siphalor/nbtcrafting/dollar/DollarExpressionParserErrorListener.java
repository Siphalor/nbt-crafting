/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting.dollar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

public class DollarExpressionParserErrorListener implements ANTLRErrorListener {
	private final List<String> inputLines;
	private final List<String> errors = new ArrayList<>();

	public DollarExpressionParserErrorListener(List<String> inputLines) {
		this.inputLines = inputLines;
	}

	public List<String> getErrors() {
		return errors;
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		StringBuilder errorBuilder = new StringBuilder();
		errorBuilder.append("Syntax error at ").append(line).append(":").append(charPositionInLine).append(": ").append(msg).append("\n");
		printErrorInputPointer(errorBuilder, line, charPositionInLine);
		errors.add(errorBuilder.toString());
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
		errors.add("Ambiguity error at " + startIndex + ":" + stopIndex);
	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
		errors.add("Attempting full context at " + startIndex + ":" + stopIndex + ": " + conflictingAlts);
	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
		errors.add("Context sensitivity at " + startIndex + ":" + stopIndex + ": " + prediction);
	}

	private void printErrorInputPointer(StringBuilder output, int line, int start) {
		output.append(inputLines.get(line - 1));
		output.append("\n");
		for (int i = 0; i < start; i++) {
			output.append(" ");
		}
		output.append("^\n");
	}
}
