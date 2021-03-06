package com.variamos.solver.core.compiler;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import com.variamos.hlcl.core.HlclProgram;
import com.variamos.hlcl.core.HlclUtil;
import com.variamos.hlcl.model.HlclFunction;
import com.variamos.hlcl.model.expressions.AssignExpression;
import com.variamos.hlcl.model.expressions.BooleanNegation;
import com.variamos.hlcl.model.expressions.BooleanOperation;
import com.variamos.hlcl.model.expressions.ComparisonExpression;
import com.variamos.hlcl.model.expressions.FunctionDeclarationExpression;
import com.variamos.hlcl.model.expressions.Identifier;
import com.variamos.hlcl.model.expressions.IntBooleanExpression;
import com.variamos.hlcl.model.expressions.IntExpression;
import com.variamos.hlcl.model.expressions.IntNumericExpression;
import com.variamos.hlcl.model.expressions.LiteralBooleanExpression;
import com.variamos.hlcl.model.expressions.NumericFloatIdentifier;
import com.variamos.hlcl.model.expressions.NumericIdentifier;
import com.variamos.hlcl.model.expressions.NumericOperation;
import com.variamos.hlcl.model.expressions.SymbolicExpression;
import com.variamos.solver.model.compiler.ConstraintSymbolsConstant;

public abstract class Hlcl2Prolog implements ConstraintSymbolsConstant {

	protected PrologTransformParameters params;

	protected List<PrologTransformParameters> paramList;

	/**
	 * Main method
	 * 
	 * @param program
	 * @return
	 */
	public String transform(HlclProgram program) {
		return transform(program, new PrologTransformParameters());
	}

	/**
	 * Used when a prolog program must be directly analyzed
	 * 
	 * @param program
	 * @param domains
	 * @return
	 */
	public String transform(HlclProgram program, List<String> domains) {

		StringBuilder out = new StringBuilder();
		writeHeaderWithDefinedDomains(program, domains, out);
		transformProgram(program, out);
		writeFooter(out);
		return out.toString();
	}

	public String transform(HlclProgram program,
			PrologTransformParameters params) {
		this.params = params;

		StringBuilder out = new StringBuilder();
		writeHeader(program, out);
		transformProgram(program, out);
		out.append(COMMA).append(LF);
		writeFooter(out);
		// System.out.println("SOLUTION: \n"+ out.toString() + "\n\n");
		return out.toString();
	}

	public String transform(HlclProgram program,
			ArrayList<PrologTransformParameters> paramList) {
		this.paramList = paramList;

		StringBuilder out = new StringBuilder();
		writeHeader(program, out);
		transformProgram(program, out);
		out.append(COMMA).append(LF);
		writeFooter(out);
		// System.out.println("SOLUTION: \n"+ out.toString() + "\n\n");
		return out.toString();
	}

	/**
	 * @param e
	 *            expression to transform
	 * @return prolog instruction that represents input expression
	 */
	public StringBuilder transformExpressionToProlog(IntExpression e) {

		StringBuilder out = new StringBuilder();
		if (e instanceof HlclProgram) {
			transformProgram((HlclProgram) e, out);
			return out;
		}
		if (e instanceof IntNumericExpression) {
			throw new RuntimeException(
					"Numeric Expression is not supported to transform");
		}
		if (e instanceof IntBooleanExpression) {
			transformBooleanExpression((IntBooleanExpression) e, out);
		}

		out.append(COMMA).append(LF);
		return out;
	}

	/*
	 * private void transformListExpression(ListDefinitionExpression e,
	 * StringBuilder out) { out.append("["); Set<Identifier> ids =
	 * HlclUtil.getUsedIdentifiers(e); writeIdentifiersList(ids, out);
	 * out.append("]"); }
	 */
	protected void transformLine(IntExpression e, StringBuilder out) {
		if (e instanceof HlclProgram) {
			transformProgram((HlclProgram) e, out);
			return;
		}
		if (e instanceof IntNumericExpression) {
			throw new RuntimeException(
					"Numeric Expression is not supported to transform");
		}
		if (e instanceof IntBooleanExpression) {
			transformBooleanExpression((IntBooleanExpression) e, out);
		}
	}

	// Abstract methods change in each prologEditor
	protected abstract void writeFooter(StringBuilder out);

	protected abstract void writeHeader(HlclProgram program, StringBuilder out);

	/**
	 * This method uses predefined domains when we analyze directly constraint
	 * programs expressed in GNU Prolog o SWI Prolog
	 * 
	 * @param program
	 * @param domainList
	 * @param out
	 */
	protected abstract void writeHeaderWithDefinedDomains(HlclProgram program,
			List<String> domainList, StringBuilder out);

	protected abstract void transformBooleanOperation(BooleanOperation e,
			StringBuilder out);

	protected void transformProgram(HlclProgram program, StringBuilder out) {

		if (program instanceof HlclFunction)
			transformFunctionDeclaration(((HlclFunction) program).getDecl(),
					out);

		int counter = 0;
		for (IntBooleanExpression e : program) {
			transformLine(e, out);
			counter++;
			if (counter < program.size())
				out.append(COMMA).append(LF);
		}
		// out.append(DOT).append(LF);
	}

	protected void transformBooleanExpression(IntBooleanExpression e,
			StringBuilder out) {
		if (e instanceof BooleanNegation) {
			transformNot((BooleanNegation) e, out);
		}
		if (e instanceof BooleanOperation) {
			transformBooleanOperation((BooleanOperation) e, out);
		}

		if (e instanceof ComparisonExpression) {
			transformComparison((ComparisonExpression) e, out);
		}

		if (e instanceof SymbolicExpression) {
			transformSymbolic((SymbolicExpression) e, out);
		}
		if (e instanceof HlclProgram) {
			transformProgram((HlclProgram) e, out);
		}
		if (e instanceof Identifier)
			transformIdentifier((Identifier) e, out);

		if (e instanceof AssignExpression)
			transformAssign((AssignExpression) e, out);

		if (e instanceof FunctionDeclarationExpression)
			transformFunctionDeclaration((FunctionDeclarationExpression) e, out);

		if (e instanceof LiteralBooleanExpression)
			out.append((((LiteralBooleanExpression) e).getPrologConstraint()));
	}

	private void transformFunctionDeclaration(FunctionDeclarationExpression e,
			StringBuilder out) {

		transformSymbolic(e.getHeader(), out);
		out.append(SPACE).append(FUNCTION_DECLARATION).append(LF);

	}

	private void transformAssign(AssignExpression e, StringBuilder out) {
		transformIdentifier(e.getIdentifier(), out);
		out.append(SPACE);
		switch (e.getType()) {
		case Assign:
			out.append(ASSIGN_VARIABLE);
			out.append(SPACE);
			// transformListExpression(
			// (ListDefinitionExpression)e.getRightExpression(), out );
			out.append(transformExpressionToProlog(e.getRightExpression()));
			break;
		case Is:
			out.append(IS);
			out.append(SPACE);
			transformNumericExpression(
					(IntNumericExpression) e.getRightExpression(), out);
			break;

		}

	}

	protected void transformNumericOperation(NumericOperation e,
			StringBuilder out) {
		out.append(OPEN_PARENTHESIS);
		transformNumericExpression(e.getLeft(), out);

		out.append(SPACE);
		switch (e.getOperator()) {
		case Diff:
			out.append(SUBSTRACTION);
			break;
		case Prod:
			out.append(MULTIPLY);
			break;
		case Sum:
			out.append(PLUS);
			break;
		}
		out.append(SPACE);
		transformNumericExpression(e.getRight(), out);
		out.append(CLOSE_PARENHESIS);
	}

	protected void transformIdentifier(Identifier e, StringBuilder out) {
		out.append(e.getId());
	}

	protected void transformNumericIdentifier(NumericIdentifier e,
			StringBuilder out) {
		out.append(e.getValue());
	}

	protected void transformNumericFloatIdentifier(NumericFloatIdentifier e,
			StringBuilder out) {
		out.append(e.getValue());
	}
	/**
	 * Method to transform a symbolic expression, this method was modified by avillota
	 * to include the transformation of global constraints with relations 
	 * @param e
	 * @param out
	 */
	protected void transformSymbolic(SymbolicExpression e, StringBuilder out) {
		if (e.getType() == SymbolicExpression.TYPE_REGULAR) {
			out.append(e.getName()).append(OPEN_PARENTHESIS);
			Set<Identifier> ids = HlclUtil.getUsedIdentifiers(e);
			writeIdentifiersList(ids, out);
			out.append(CLOSE_PARENHESIS);
		}else {
			out.append(e.getName()).append(OPEN_PARENTHESIS);
			out.append(SPACE);
			out.append(OPEN_BRACKET);
			out.append(OPEN_BRACKET);
			Set<Identifier> ids = HlclUtil.getUsedIdentifiers(e);
			writeIdentifiersList(ids, out);
			out.append(CLOSE_BRACKET);
			out.append(CLOSE_BRACKET);
			out.append(COMMA);
			writeTuples(e.getTuples(), out);
			out.append(CLOSE_PARENHESIS);
		}

	}

	protected void transformNumericExpression(IntNumericExpression e,
			StringBuilder out) {

		if (e instanceof Identifier)
			transformIdentifier((Identifier) e, out);

		if (e instanceof NumericIdentifier)
			transformNumericIdentifier((NumericIdentifier) e, out);

		// jcmunoz added for float support
		if (e instanceof NumericFloatIdentifier)
			transformNumericFloatIdentifier((NumericFloatIdentifier) e, out);

		if (e instanceof NumericOperation) {
			transformNumericOperation((NumericOperation) e, out);
		}
	}

	protected void transformComparison(ComparisonExpression e, StringBuilder out) {
		if (e.getLeft() instanceof IntNumericExpression)
			transformNumericExpression((IntNumericExpression) e.getLeft(), out);
		else
			transformBooleanExpression((IntBooleanExpression) e.getLeft(), out);
		out.append(SPACE);
		switch (e.getType()) {
		case Equals:
			out.append(EQUALS);
			break;
		case GreaterOrEqualsThan:
			out.append(MORE_OR_EQUALS);
			break;
		case GreaterThan:
			out.append(MORE);
			break;
		case LessOrEqualsThan:
			out.append(LESS_OR_EQUALS);
			break;
		case LessThan:
			out.append(LESS);
			break;
		case NotEquals:
			out.append(NOT_EQUALS);
			break;
		}
		out.append(SPACE);
		if (e.getRight() instanceof IntNumericExpression)
			transformNumericExpression((IntNumericExpression) e.getRight(), out);
		else
			transformBooleanExpression((IntBooleanExpression) e.getRight(), out);
	}

	protected void transformNot(BooleanNegation e, StringBuilder out) {
		out.append(ONE).append(SPACE).append(SUBSTRACTION).append(SPACE)
				.append(OPEN_PARENTHESIS);
		transformBooleanExpression(e.getExpression(), out);
		out.append(CLOSE_PARENHESIS).append(SPACE).append(MORE).append(SPACE)
				.append(ZERO);
	}

	protected void writeIdentifiersList(Set<Identifier> ids, StringBuilder out) {
		int i = 0;
		for (Identifier id : ids) {
			out.append(id.getId());
			i++;
			if (i < ids.size())
				out.append(COMMA + " ");
		}
	}

	public void writeList(List<String> ids, StringBuilder out) {
		int i = 0;
		for (String id : ids) {
			out.append(id);
			i++;
			if (i < ids.size())
				out.append(COMMA + " ");
		}
	}

	public void writeIdentifiersList(List<Identifier> ids, StringBuilder out) {
		int i = 0;
		for (Identifier id : ids) {
			out.append(id.getId());
			i++;
			if (i < ids.size())
				out.append(COMMA + " ");
		}
	}
	/**
	 * Method to create a list of list containing numeric values 
	 * @author avillota
	 * @param tuples
	 * @param out
	 */
	public void writeTuples(NumericIdentifier[][] tuples, StringBuilder out){
		String list=OPEN_BRACKET;
		
		String firstList= OPEN_BRACKET+ tuples[0][0].getValue();
		for (int j = 1; j < tuples[0].length; j++) {
			firstList+= ","+ SPACE+ tuples[0][j].getValue();
		}
		firstList+=CLOSE_BRACKET;
		
		list+= firstList;
		
		for (int i = 1; i < tuples.length; i++) {
			String innerList= OPEN_BRACKET + tuples[i][0].getValue();
			for (int j = 1; j < tuples[0].length; j++) {
				innerList+= ","+ SPACE+ tuples[i][j].getValue();
			}
			innerList+=CLOSE_BRACKET;
			list+=","+ SPACE + innerList;
		}
		list+= CLOSE_BRACKET;
		out.append(list);
	}
}
