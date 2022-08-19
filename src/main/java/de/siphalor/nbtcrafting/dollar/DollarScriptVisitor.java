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

import java.util.List;

import net.minecraft.util.Pair;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.siphalor.nbtcrafting.dollar.antlr.DollarScriptParser;
import de.siphalor.nbtcrafting.dollar.antlr.DollarScriptParserBaseVisitor;
import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.function.DollarFunction;
import de.siphalor.nbtcrafting.dollar.function.DollarFunctions;
import de.siphalor.nbtcrafting.dollar.part.DollarBinding;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.binary.*;
import de.siphalor.nbtcrafting.dollar.part.special.DollarStatementList;
import de.siphalor.nbtcrafting.dollar.part.special.FunctionCallDollarPart;
import de.siphalor.nbtcrafting.dollar.part.ternary.ConditionDollarOperator;
import de.siphalor.nbtcrafting.dollar.part.unary.NegationDollarOperator;
import de.siphalor.nbtcrafting.dollar.part.unary.NotDollarOperator;
import de.siphalor.nbtcrafting.dollar.part.value.ListConstructDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ObjectConstructDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ReferenceDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class DollarScriptVisitor extends DollarScriptParserBaseVisitor<DollarPart> {
	@Override
	public DollarPart visitNumber(DollarScriptParser.NumberContext ctx) {
		TerminalNode node = ctx.INTEGER_LITERAL();
		if (node != null) {
			return ValueDollarPart.of(NumberUtil.toSmallestInteger(Long.parseLong(node.getText())));
		}
		node = ctx.FLOAT_LITERAL();
		return ValueDollarPart.of(Double.parseDouble(node.getText()));
	}

	@Override
	public DollarPart visitStringLiteral(DollarScriptParser.StringLiteralContext ctx) {
		String text = ctx.getText();
		return ValueDollarPart.of(text.substring(1, text.length() - 1));
	}

	@Override
	public DollarPart visitIdentifier(DollarScriptParser.IdentifierContext ctx) {
		return ReferenceDollarPart.of(ctx.getText());
	}

	@Override
	public DollarPart visitNamedConstant(DollarScriptParser.NamedConstantContext ctx) {
		switch (ctx.start.getType()) {
			case DollarScriptParser.TRUE:
				return ValueDollarPart.of(true);
			case DollarScriptParser.FALSE:
				return ValueDollarPart.of(false);
			case DollarScriptParser.NULL:
				return ValueDollarPart.of(null);
			default:
				throw new RuntimeException("Unknown named constant: " + ctx.start.getText());
		}
	}

	@Override
	public DollarPart visitNesting(DollarScriptParser.NestingContext ctx) {
		return this.visit(ctx.expr());
	}

	@Override
	public DollarPart visitListConstruct(DollarScriptParser.ListConstructContext ctx) {
		try {
			return ListConstructDollarPart.of(ctx.expr().stream().map(this::visit).toArray(DollarPart[]::new));
		} catch (DollarDeserializationException e) {
			throw new RuntimeException("Failed to read list construct at " + ctx.getStart().getLine() + ": " + ctx.getStart().getCharPositionInLine());
		}
	}

	@Override
	public DollarPart visitObjectConstruct(DollarScriptParser.ObjectConstructContext ctx) {
		try {
			//noinspection unchecked
			return ObjectConstructDollarPart.of(ctx.objectConstructProperty().stream().map(prop ->
					new Pair<>(prop.identifier().getText(), this.visit(prop.expr()))
			).toArray(Pair[]::new));
		} catch (DollarDeserializationException e) {
			throw new RuntimeException("Failed to read object construct at " + ctx.getStart().getLine() + ": " + ctx.getStart().getCharPositionInLine());
		}
	}

	@Override
	public DollarPart visitLambda(DollarScriptParser.LambdaContext ctx) {
		List<DollarScriptParser.IdentifierContext> identifierContexts = ctx.identifier();
		String[] parameters = new String[identifierContexts.size()];
		for (int i = 0; i < identifierContexts.size(); i++) {
			parameters[i] = identifierContexts.get(i).getText();
		}
		if (ctx.inline != null) {
			return ValueDollarPart.of(new DollarLambda(parameters, this.visitExpr(ctx.inline)));
		} else {
			return ValueDollarPart.of(new DollarLambda(parameters, this.visitStatementList(ctx.body)));
		}
	}

	@Override
	public DollarPart visitFunctionCall(DollarScriptParser.FunctionCallContext ctx) {
		String identifier = ctx.identifier().getText();
		DollarFunction dollarFunction = DollarFunctions.get(identifier);
		if (dollarFunction != null) {
			try {
				return FunctionCallDollarPart.of(dollarFunction, ctx.expr().stream().map(this::visit).toArray(DollarPart[]::new));
			} catch (DollarDeserializationException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Unknown function \"" + identifier + "\" at " + ctx.getStart().getLine() + ":" + ctx.getStart().getCharPositionInLine());
		}
	}

	@Override
	public DollarBinding visitLexpr(DollarScriptParser.LexprContext ctx) {
		if (ctx.op == null) {
			return new ReferenceDollarPart(ctx.identifier().getText());
		}
		DollarBinding left = this.visitLexpr(ctx.lexpr());
		switch (ctx.op.getType()) {
			case DollarScriptParser.DOT:
				return new ChildDollarOperator(left, ValueDollarPart.of(ctx.identifier().getText()));
			case DollarScriptParser.LBRACK:
				DollarScriptParser.ExprContext keyExpr = ctx.expr();
				if (keyExpr == null) {
					return new ChildDollarOperator(left, null);
				}
				return new ChildDollarOperator(left, this.visitExpr(keyExpr));
			default:
				throw new RuntimeException("Unsupported operator: " + ctx.op.getText());
		}
	}

	@Override
	public DollarPart visitAssignmentExpr(DollarScriptParser.AssignmentExprContext ctx) {
		return AssignmentDollarPart.of(this.visitLexpr(ctx.lexpr()), this.visitExpr(ctx.expr()));
	}

	@Override
	public DollarPart visitExpr(DollarScriptParser.ExprContext ctx) {
		if (ctx.op == null) {
			if (ctx.getChildCount() == 0) {
				return defaultResult();
			}

			return this.visit(ctx.getChild(0));
		}

		try {
			List<DollarScriptParser.ExprContext> expressions = ctx.expr();
			switch (ctx.op.getType()) {
				case DollarScriptParser.DOT:
					return ChildDollarOperator.of(this.visitExpr(expressions.get(0)), ValueDollarPart.of(ctx.identifier().getText()));
				case DollarScriptParser.LBRACK:
					return ChildDollarOperator.of(this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.HASH:
					return CastDollarOperator.of(this.visitExpr(expressions.get(0)), ctx.HASH_CAST().getText().charAt(1));
				case DollarScriptParser.BANG:
					return NotDollarOperator.of(this.visitExpr(expressions.get(0)));
				case DollarScriptParser.MINUS:
					if (expressions.size() == 1) {
						return NegationDollarOperator.of(this.visitExpr(expressions.get(0)));
					}
					return NumericBinaryDollarOperator.differenceOf(this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.PLUS:
					if (expressions.size() == 1) {
						return CastDollarOperator.of(this.visitExpr(expressions.get(0)), 'n');
					}
					return SumDollarOperator.of(this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.DIVIDE:
					return NumericBinaryDollarOperator.of(NumberUtil::quotient, this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.MULTIPLY:
					return NumericBinaryDollarOperator.of(NumberUtil::product, this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.EQUAL:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.EQUAL, this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.NOT_EQUAL:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.NOT_EQUAL, this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.LESS:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.LESS, this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.LESS_EQUAL:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.LESS_OR_EQUAL, this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.GREATER:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.GREATER, this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.GREATER_EQUAL:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.GREATER_OR_EQUAL, this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.LOG_AND:
					return LogicalBinaryDollarOperator.andOf(this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.LOG_OR:
					return LogicalBinaryDollarOperator.orOf(this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)));
				case DollarScriptParser.QUEST:
					return ConditionDollarOperator.of(this.visitExpr(expressions.get(0)), this.visitExpr(expressions.get(1)), this.visitExpr(expressions.get(2)));
			}
		} catch (DollarDeserializationException e) {
			throw new RuntimeException("Failed to read dollar expression at " + ctx.op.getCharPositionInLine() + ":" + ctx.op.getLine(), e);
		}
		return null;
	}

	@Override
	public DollarPart visitStatementList(DollarScriptParser.StatementListContext ctx) {
		List<DollarScriptParser.StatementContext> statementContexts = ctx.statement();
		DollarPart[] statements = new DollarPart[statementContexts.size()];
		for (int i = 0; i < statementContexts.size(); i++) {
			statements[i] = this.visitStatement(statementContexts.get(i));
		}
		return DollarStatementList.of(statements);
	}
}
