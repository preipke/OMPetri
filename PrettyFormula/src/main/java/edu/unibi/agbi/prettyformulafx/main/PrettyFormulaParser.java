package edu.unibi.agbi.prettyformulafx.main;

import edu.unibi.agbi.prettyformulafx.antlr.FormulaLexer;
import edu.unibi.agbi.prettyformulafx.antlr.FormulaParser;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

/**
 *
 * @author Martin Zurowietz
 */
public class PrettyFormulaParser {

    private static final PrettyFormulaParseRules EXTRACTOR = new PrettyFormulaParseRules();
    
    /**
     * Parses a mathematical formula String like "(a+b)/c" to a pretty image.
     *
     * @param formula A raw formula input String.
     * @return An image object containing the rendered formula.
     * @throws ParseException When the formula rendering fails.
     * @throws DetailedParseCancellationException when the formula parsing
     * fails.
     */
    public static BufferedImage parseToImage(String formula) throws ParseException, DetailedParseCancellationException {

        String latexFormula = PrettyFormulaParser.parseToLatex(formula);
        TeXIcon icon = PrettyFormulaParser.getTeXIcon(latexFormula);

        // now create an actual image of the rendered equation
        BufferedImage image = new BufferedImage(icon.getIconWidth(),
                icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
        icon.paintIcon(null, g2, 0, 0);

        return image;
    }

    /**
     * Parses a mathematical formula String like "(a+b)/c" to valid math LaTeX.
     *
     * @param formula A raw formula input String.
     * @return The formula parsed to a small subset of LaTeX.
     * @throws DetailedParseCancellationException When the parsing fails.
     */
    public static String parseToLatex(String formula) throws DetailedParseCancellationException {
        
        FormulaLexer lexer = new FormulaLexer(new ANTLRInputStream(formula));
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        FormulaParser parser = new FormulaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        ParserRuleContext tree = parser.expr();

        return PrettyFormulaParser.EXTRACTOR.visit(tree);
    }

    /**
     * Renders a valid LaTeX math formula to an icon.
     *
     * @param formula Valid LaTeX formula.
     * @return Rendered Icon.
     * @throws ParseException When rendering has failed (e.g. the formula was
     * not valid).
     */
    private static TeXIcon getTeXIcon(String formula) throws ParseException {
        
        TeXFormula latexFormula = new TeXFormula(formula);

        // render the formla to an icon of the same size as the formula.
        TeXIcon icon = latexFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);

        // insert a border 
        icon.setInsets(new Insets(5, 5, 5, 5));

        return icon;
    }
}
