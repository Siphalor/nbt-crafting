package de.siphalor.nbtcrafting.dollars;

public enum DollarOperator {
	MULTIPLY, DIVIDE, ADD, SUBTRACT;

	public static Object executeOperator(Object first, DollarOperator operator, Object second) throws DollarException {
		switch(operator) {
			case ADD:
				if(first == null) {
					if(second instanceof Double)
						return (Double) second;
					else if(second instanceof String)
						return (String) second;
				} else if(first instanceof Double) {
					if(second instanceof Double) {
						return (Double) first + (Double) second;
					} else
						throw new DollarException("type conflict at '+'");
				} else if(first instanceof String) {
					return (String) first + second.toString();
				}
				break;
			case SUBTRACT:
				if(first instanceof Double && second instanceof Double) {
					return (Double) first + (Double) second;
				} else
					throw new DollarException("type conflict at '-'");
			case MULTIPLY:
				if(first instanceof Double && second instanceof Double) {
					return (Double) first * (Double) second;
				} else
					throw new DollarException("type conflict at '*'");
			case DIVIDE:
				if(first instanceof Double && second instanceof Double) {
					return (Double) first / (Double) second;
				} else
					throw new DollarException("type conflict at '/'");
		}
		return second;
	}
}
