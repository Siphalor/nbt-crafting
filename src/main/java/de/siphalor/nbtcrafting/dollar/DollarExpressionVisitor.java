package de.siphalor.nbtcrafting.dollar;

import java.util.function.Function;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import de.siphalor.nbtcrafting.dollar.antlr.DollarParser;
import de.siphalor.nbtcrafting.dollar.antlr.DollarParserVisitor;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class DollarExpressionVisitor implements DollarParserVisitor<DollarPart> {
	private final Function<String, Object> referenceResolver;

	public DollarExpressionVisitor(Function<String, Object> referenceResolver) {
		this.referenceResolver = referenceResolver;
	}

	@Override
	public DollarPart visitNumber(DollarParser.NumberContext ctx) {
		TerminalNode node = ctx.INTEGER_LITERAL();
		if (node != null) {
			return ValueDollarPart.of(NumberUtil.toSmallestInteger(Long.parseLong(node.getText())));
		}
		node = ctx.FLOAT_LITERAL();
		return ValueDollarPart.of(Double.parseDouble(node.getText()));
	}

	@Override
	public DollarPart visitLiteral(DollarParser.LiteralContext ctx) {
		return ValueDollarPart.of(ctx.getText());
	}

	@Override
	public DollarPart visitIdentifier(DollarParser.IdentifierContext ctx) {
		return ValueDollarPart.of(ctx.getText());
	}

	@Override
	public DollarPart visitComparison_operator(DollarParser.Comparison_operatorContext ctx) {
		return null;
	}

	@Override
	public DollarPart visitNesting(DollarParser.NestingContext ctx) {
		return null;
	}

	@Override
	public DollarPart visitFunction_args(DollarParser.Function_argsContext ctx) {
		return null;
	}

	@Override
	public DollarPart visitFunction_call(DollarParser.Function_callContext ctx) {
		return null;
	}

	@Override
	public DollarPart visitOperand(DollarParser.OperandContext ctx) {
		return null;
	}

	@Override
	public DollarPart visitExpr(DollarParser.ExprContext ctx) {
		return null;
	}

	@Override
	public DollarPart visit(ParseTree tree) {
		return null;
	}

	@Override
	public DollarPart visitChildren(RuleNode node) {
		return null;
	}

	@Override
	public DollarPart visitTerminal(TerminalNode node) {
		return null;
	}

	@Override
	public DollarPart visitErrorNode(ErrorNode node) {
		return null;
	}
}
