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

import org.antlr.v4.runtime.tree.TerminalNode;

import de.siphalor.nbtcrafting.dollar.antlr.DollarExpressionParser;
import de.siphalor.nbtcrafting.dollar.antlr.DollarExpressionParserBaseVisitor;
import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.binary.*;
import de.siphalor.nbtcrafting.dollar.part.ternary.ConditionDollarOperator;
import de.siphalor.nbtcrafting.dollar.part.unary.NegationDollarOperator;
import de.siphalor.nbtcrafting.dollar.part.unary.NotDollarOperator;
import de.siphalor.nbtcrafting.dollar.part.value.ReferenceDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class DollarExpressionVisitor extends DollarExpressionParserBaseVisitor<DollarPart> {
	@Override
	public DollarPart visitNumber(DollarExpressionParser.NumberContext ctx) {
		TerminalNode node = ctx.INTEGER_LITERAL();
		if (node != null) {
			return ValueDollarPart.of(NumberUtil.toSmallestInteger(Long.parseLong(node.getText())));
		}
		node = ctx.FLOAT_LITERAL();
		return ValueDollarPart.of(Double.parseDouble(node.getText()));
	}

	@Override
	public DollarPart visitLiteral(DollarExpressionParser.LiteralContext ctx) {
		String text = ctx.getText();
		return ValueDollarPart.of(text.substring(1, text.length() - 1));
	}

	@Override
	public DollarPart visitIdentifier(DollarExpressionParser.IdentifierContext ctx) {
		return ReferenceDollarPart.of(ctx.getText());
	}

	@Override
	public DollarPart visitNesting(DollarExpressionParser.NestingContext ctx) {
		return this.visit(ctx.expr());
	}

	@Override
	public DollarPart visitFunction_args(DollarExpressionParser.Function_argsContext ctx) {
		return null;
	}

	@Override
	public DollarPart visitFunction_call(DollarExpressionParser.Function_callContext ctx) {
		return null;
	}

	@Override
	public DollarPart visitExpr(DollarExpressionParser.ExprContext ctx) {
		if (ctx.op == null) {
			if (ctx.getChildCount() == 0) {
				return defaultResult();
			}

			return this.visit(ctx.getChild(0));
		}

		try {
			List<DollarExpressionParser.ExprContext> expressions = ctx.expr();
			switch (ctx.op.getType()) {
				case DollarExpressionParser.DOT:
					return ChildDollarOperator.of(this.visit(expressions.get(0)), ValueDollarPart.of(ctx.identifier().getText()));
				case DollarExpressionParser.LBRACK:
					return ChildDollarOperator.of(this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.HASH:
					return CastDollarOperator.of(this.visit(expressions.get(0)), ctx.TYPE_INDICATOR().getText().charAt(0));
				case DollarExpressionParser.BANG:
					return NotDollarOperator.of(this.visit(expressions.get(0)));
				case DollarExpressionParser.MINUS:
					if (expressions.size() == 1) {
						return NegationDollarOperator.of(this.visit(expressions.get(0)));
					}
					return NumericBinaryDollarOperator.differenceOf(this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.PLUS:
					if (expressions.size() == 1) {
						return CastDollarOperator.of(this.visit(expressions.get(0)), 'n');
					}
					return SumDollarOperator.of(this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.EQUAL:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.EQUAL, this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.NOT_EQUAL:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.NOT_EQUAL, this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.LESS:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.LESS, this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.LESS_EQUAL:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.LESS_OR_EQUAL, this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.GREATER:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.GREATER, this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.GREATER_EQUAL:
					return ComparisonDollarOperator.of(ComparisonDollarOperator.Type.GREATER_OR_EQUAL, this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.LOG_AND:
					return LogicalBinaryDollarOperator.andOf(this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.LOG_OR:
					return LogicalBinaryDollarOperator.orOf(this.visit(expressions.get(0)), this.visit(expressions.get(1)));
				case DollarExpressionParser.QUEST:
					return ConditionDollarOperator.of(this.visit(expressions.get(0)), this.visit(expressions.get(1)), this.visit(expressions.get(2)));
			}
		} catch (DollarDeserializationException e) {
			throw new RuntimeException("Failed to read dollar expression at " + ctx.op.getCharPositionInLine() + ":" + ctx.op.getLine(), e);
		}
		return null;
	}

	@Override
	public DollarPart visitStatement(DollarExpressionParser.StatementContext ctx) {
		return this.visit(ctx.expr());
	}
}
