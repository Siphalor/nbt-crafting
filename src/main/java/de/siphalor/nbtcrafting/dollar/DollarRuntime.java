package de.siphalor.nbtcrafting.dollar;

import java.util.Stack;
import java.util.function.Function;

import de.siphalor.nbtcrafting.dollar.instruction.Instruction;

public class DollarRuntime {
	private final Function<String, Object> referenceResolver;

	public DollarRuntime(Function<String, Object> referenceResolver) {
		this.referenceResolver = referenceResolver;
	}

	public Object run(Instruction[] instructions) throws DollarEvaluationException {
		Context context = new Context();

		while (context.instructionPointer < instructions.length) {
			Instruction instruction = instructions[context.instructionPointer];
			instruction.apply(context.stack, context);
			context.instructionPointer++;
		}

		if (context.stack.isEmpty()) {
			throw new DollarEvaluationException("Stack is empty after evaluation");
		}

		Object result = context.stack.pop();
		if (!context.stack.isEmpty()) {
			throw new DollarEvaluationException("Stack is not empty after evaluation");
		}
		return result;
	}

	public class Context {
		private final Stack<Object> stack = new Stack<>();
		private int instructionPointer = 0;

		public Object resolveReference(String reference) {
			return referenceResolver.apply(reference);
		}

		public void jumpOffset(int offset) {
			instructionPointer += offset;
		}
	}
}
