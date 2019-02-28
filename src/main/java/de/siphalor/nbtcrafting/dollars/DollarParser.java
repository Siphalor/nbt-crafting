package de.siphalor.nbtcrafting.dollars;

import org.apache.commons.lang3.StringUtils;

public class DollarParser {
	private String expression;

	public Dollar parse(String key, String value) {
        Dollar dollar = new Dollar(key);
		try {
			this.expression = value;
			dollar.expression = parse();
		} catch (DollarException e) {
			e.printStackTrace();
		}
		return dollar;
	}

	private GroupDollarPart parse() throws DollarException {
		GroupDollarPart groupPart = new GroupDollarPart();
		groupPart.parts.add(parsePart());
		while(!expression.equals("")) {
			if(StringUtils.isWhitespace(expression.substring(0, 1))) {
				eatTo(1);
				if(expression.equals("")) throw new DollarException("Illegal whitespacey statement");
			}
			switch(expression.charAt(0)) {
				case '+':
					groupPart.operators.add(DollarOperator.ADD);
					break;
				case '-':
					groupPart.operators.add(DollarOperator.SUBTRACT);
					break;
				case '*':
					groupPart.operators.add(DollarOperator.MULTIPLY);
					break;
				case '/':
					groupPart.operators.add(DollarOperator.DIVIDE);
					break;
			}
            eatTo(1);
			groupPart.parts.add(parsePart());
		}
		return groupPart;
	}

	private DollarPart parsePart() throws DollarException {
		if(StringUtils.isWhitespace(expression.substring(0, 1))) {
            eatTo(1);
			if(expression.equals("")) throw new DollarException("Illegal whitespacey statement");
		}
		if(expression.matches("-?\\d*\\.?\\d+.*")) {
			int index = StringUtils.indexOfAny(expression.substring(1), "+-*/");
			if(index == -1)
				index = expression.length();
			ValueDollarPart value = new ValueDollarPart(Double.parseDouble(expression.substring(0, index)));
            eatTo(index);
			return value;
		}
		int index = StringUtils.indexOfAny(expression, " \n\t\r+-*/");
		if(!expression.substring(0, index).matches("[\\w\\d.]+"))
			throw new DollarException("Illegal statement: " + expression);
		DollarPart part = new ReferenceDollarPart(expression.substring(0, index));
        eatTo(index);
		return part;
	}

	private void eatTo(int index) {
		expression = expression.substring(index);
	}
}
