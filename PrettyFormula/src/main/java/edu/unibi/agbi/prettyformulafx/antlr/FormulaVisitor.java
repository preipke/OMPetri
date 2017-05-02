package edu.unibi.agbi.prettyformulafx.antlr;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FormulaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 * 
 * @author Martin Zurowietz
 */
public interface FormulaVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FormulaParser #number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(@NotNull FormulaParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser #function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(@NotNull FormulaParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser #neg_number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNeg_number(@NotNull FormulaParser.Neg_numberContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser #variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(@NotNull FormulaParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser #expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(@NotNull FormulaParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser #term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(@NotNull FormulaParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser #atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(@NotNull FormulaParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link FormulaParser #neg_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNeg_variable(@NotNull FormulaParser.Neg_variableContext ctx);
}
