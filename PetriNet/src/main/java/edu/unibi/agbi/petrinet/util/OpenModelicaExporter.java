/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.util;

import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.FunctionElement;
import edu.unibi.agbi.petrinet.model.PetriNet;
import edu.unibi.agbi.petrinet.model.References;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Properties;

/**
 *
 * @author PR
 */
public class OpenModelicaExporter
{
    private static final String ENDL = System.getProperty("line.separator");
    private static final String INDENT = "  ";

    private final String propertiesPath = "/openmodelica.properties";
    private final Properties properties;
    
    public OpenModelicaExporter() throws IOException {
        properties = new Properties();
        properties.load(OpenModelicaExporter.class.getResourceAsStream(propertiesPath));
    }

    public References export(PetriNet petriNet, File fileMOS, File fileMO, File workDirectory) throws IOException {
        fileMO = exportMO(petriNet, fileMO);
        return exportMOS(petriNet, fileMOS, fileMO, workDirectory);
    }

    public File exportMO(PetriNet model, File file) throws IOException {

        PrintWriter writer = new PrintWriter(file);

        Collection<Arc> arcs = model.getArcs();
        Collection<Colour> colors = model.getColours();
        Collection<Place> places = model.getPlaces();
        Collection<Transition> transitions = model.getTransitions();

        boolean isColoredPn = colors.size() != 1;

        /**
         * Model name.
         */
        writer.append("model '" + model.getName() + "'");
        writer.println();

        /**
         * Functions.
         */
        if (isColoredPn) {
            writer.append("function g1" + ENDL + "    input Real[2] inColors;" + ENDL + "    output Real[2] outWeights;" + ENDL + "  algorithm" + ENDL
                    + "    if sum(inColors) < 1e-12 then" + ENDL + "      outWeights := fill(1, 2);" + ENDL + "    else" + ENDL
                    + "      outWeights[1] := inColors[1] / sum(inColors);" + ENDL + "      outWeights[2] := inColors[2] / sum(inColors);" + ENDL + "    end if;" + ENDL
                    + "  end g1;" + ENDL + "  function g2" + ENDL + "    input Real[2] inColors1;" + ENDL + "    input Real[2] inColors2;" + ENDL + "    output Real[2] outWeights;" + ENDL
                    + "  algorithm" + ENDL + "    if sum(inColors1) < 1e-12 then" + ENDL + "      outWeights := fill(0.5, 2);" + ENDL + "    else" + ENDL
                    + "      outWeights[1] := inColors1[1] / sum(inColors1) / 2;" + ENDL + "      outWeights[2] := inColors1[2] / sum(inColors1) / 2;" + ENDL
                    + "    end if;" + ENDL + "" + ENDL + "    if sum(inColors2) < 1e-12 then" + ENDL + "      outWeights[1] := outWeights[1] + 0.5;" + ENDL
                    + "      outWeights[2] := outWeights[2] + 0.5;" + ENDL + "    else" + ENDL
                    + "      outWeights[1] := outWeights[1] + inColors2[1] / sum(inColors2) / 2;" + ENDL
                    + "      outWeights[2] := outWeights[2] + inColors2[2] / sum(inColors2) / 2;" + ENDL + "    end if;" + ENDL + "  end g2;" + ENDL);
            writer.println();
        }

        /**
         * Settings.
         */
        writer.append(INDENT + "inner PNlib.Settings");
        writer.append(" settings(showTokenFlow = true)");
        writer.append(";");
        writer.println();

        /**
         * Places.
         */
        boolean isFirst, isInnerFirst;
        String tmp1, tmp2, tmp3, tokenType, functionType, functionString;
        Function function;
        Token token;
        Weight weight;
        INode arcSource, arcTarget;
        int index;

        for (Place place : places) {

            tmp1 = "";
            tmp2 = "";
            tmp3 = "";

            writer.append(INDENT);
            switch (place.getPlaceType()) {
                
                case CONTINUOUS:
                    if (isColoredPn) {
                        writer.append(properties.getProperty("pnlib.colored.place.continuous"));
                    } else {
                        writer.append(properties.getProperty("pnlib.place.continuous"));
                    }
                    tokenType = "Marks";
                    break;
                    
                case DISCRETE:
                    if (isColoredPn) {
                        writer.append(properties.getProperty("pnlib.colored.place.discrete"));
                        throw new IOException("Colored discrete places are not yet supported!"); // TODO
                    } else {
                        writer.append(properties.getProperty("pnlib.place.discrete"));
                    }
                    tokenType = "Tokens";
                    break;
                    
                default:
                    throw new IOException("Unhandled place type detected! [" + place.getPlaceType() + "]");
            }
            writer.append(" '" + place.getId() + "'");
            writer.append("(nIn=" + place.getArcsIn().size());
            writer.append(",nOut=" + place.getArcsOut().size());

            isFirst = true;

            /**
             * Token
             */
            for (Colour color : colors) {

                token = place.getToken(color);

                if (isColoredPn) {

                    if (isFirst) {
                        isFirst = false;
                    } else {
                        tmp1 += ",";
                        tmp2 += ",";
                        tmp3 += ",";
                    }

                    if (token != null) {
                        if (place.getPlaceType() == Place.Type.CONTINUOUS) {
                            tmp1 += token.getValueStart();
                            tmp2 += token.getValueMin();
                            tmp3 += token.getValueMax();
                        } else {
                            tmp1 += (int) token.getValueStart();
                            tmp2 += (int) token.getValueMin();
                            tmp3 += (int) token.getValueMax();
                        }
                    } else {
                        tmp1 += "0";
                        tmp2 += "0";
                        tmp3 += "0";
                    }

                } else {

                    if (place.getPlaceType() == Place.Type.CONTINUOUS) {
                        tmp1 += token.getValueStart();
                        tmp2 += token.getValueMin();
                        tmp3 += token.getValueMax();
                    } else {
                        tmp1 += (int) token.getValueStart();
                        tmp2 += (int) token.getValueMin();
                        tmp3 += (int) token.getValueMax();
                    }
                }

            }

            if (isColoredPn) {
                tmp1 = "{" + tmp1 + "}";
                tmp2 = "{" + tmp2 + "}";
                tmp3 = "{" + tmp3 + "}";
            }

            writer.append(",start" + tokenType + "=" + tmp1);
            writer.append(",min" + tokenType + "=" + tmp2);
            writer.append(",max" + tokenType + "=" + tmp3);

//            writer.append(",t(final unit=\"custom unit\")");
            writer.append(")");
//            writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
            writer.append(";");
            writer.println();
        }

        /**
         * Transitions.
         */
        for (Transition transition : transitions) {

            writer.append(INDENT);
            switch (transition.getTransitionType()) {
                
                case CONTINUOUS:
                    if (isColoredPn) {
                        writer.append(properties.getProperty("pnlib.colored.transition.continuous"));
                        functionType = "maximumSpeed";
                    } else {
                        writer.append(properties.getProperty("pnlib.transition.continuous"));
                        functionType = "maximumSpeed";
                    }
                    break;
                    
                case DISCRETE:
                    if (isColoredPn) {
                        writer.append(properties.getProperty("pnlib.colored.transition.discrete"));
                        throw new IOException("Colored discrete transitions are not yet supported!");
                    } else {
                        writer.append(properties.getProperty("pnlib.transition.discrete"));
                        functionType = "delay";
                    }
                    break;
                    
                case STOCHASTIC:
                    if (isColoredPn) {
                        writer.append(properties.getProperty("pnlib.colored.transition.stochastic"));
                        throw new IOException("Colored stochastic transitions are not yet supported!");
                    } else {
                        writer.append(properties.getProperty("pnlib.transition.stochastic"));
                        functionType = "h"; // TODO
                    }
                    break;
                    
                default:
                    throw new IOException("Unhandelt transition type detected! [" + transition.getTransitionType() + "]");
            }
            writer.append(" '" + transition.getId() + "'");
            writer.append("(nIn=" + transition.getArcsIn().size());
            writer.append(",nOut=" + transition.getArcsOut().size());
            writer.append("," + functionType);
            
            function = transition.getFunction();
            if (!function.getUnit().isEmpty()) {
                writer.append("(final unit=\"" + function.getUnit() + "\")");
            }
            
            // Function and parameters
            functionString = "";
            for (FunctionElement element : function.getElements()) {
                if (element.getType() == FunctionElement.Type.PARAMETER) {
                    functionString += model.getParameter(element.get()).getValue();
                } else {
                    functionString += element.get();
                }
            }
            writer.append("=" + functionString);

            /**
             * Weights, incoming
             */
            tmp1 = "";
            isFirst = true;

            for (IArc arc : transition.getArcsIn()) {

                if (isFirst) {
                    isFirst = false;
                } else {
                    tmp1 += ",";
                }

                tmp3 = "";
                isInnerFirst = true;

                for (Colour color : colors) {

                    weight = arc.getWeight(color);

                    if (isInnerFirst) {
                        isInnerFirst = false;
                    } else {
                        tmp3 += ",";
                    }

                    if (isColoredPn) {

                        if (weight != null) {
                            tmp3 += weight.getValue();
                        } else {
                            tmp3 += "0";
                        }

                    } else {
                        tmp1 += weight.getValue();
                    }

                }

                if (isColoredPn) {
                    tmp1 += "{" + tmp3 + "}/*" + arc.getSource().getId() + "*/";
                }

            }

            /**
             * Weights, outgoing
             */
            tmp2 = "";
            isFirst = true;

            for (IArc arc : transition.getArcsOut()) {

                if (isFirst) {
                    isFirst = false;
                } else {
                    tmp2 += ",";
                }

                tmp3 = "";
                isInnerFirst = true;

                for (Colour color : colors) {

                    weight = arc.getWeight(color);

                    if (isInnerFirst) {
                        isInnerFirst = false;
                    } else {
                        tmp3 += ",";
                    }

                    if (isColoredPn) {

                        if (weight != null) {
                            tmp3 += weight.getValue();
                        } else {
                            tmp3 += "0";
                        }

                    } else {
                        tmp2 += weight.getValue();
                    }

                }

                if (isColoredPn) {

                    tmp2 += "{" + tmp3 + "}/*" + arc.getSource().getId() + "*/";

                }

            }

            writer.append(",arcWeightIn={" + tmp1 + "}");
            writer.append(",arcWeightOut={" + tmp2 + "}");
            writer.append(")");
//            writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
            writer.append(";");
            writer.println();
        }

        /**
         * Connections.
         */
        writer.append("equation");
        writer.println();

        for (IArc a : arcs) {

            writer.append(INDENT);
            writer.append("connect(");

            arcSource = a.getSource();
            arcTarget = a.getTarget();

            index = 1;
            for (IArc arc : arcSource.getArcsOut()) {
                if (arcTarget.equals(arc.getTarget())) {
                    break;
                }
                index++;
            }
            if (arcSource instanceof Place) {
                writer.append("'" + arcSource.getId() + "'.outTransition[" + index + "],");
            } else {
                writer.append("'" + arcSource.getId() + "'.outPlaces[" + index + "],");
            }

            index = 1;
            for (IArc arc : arcTarget.getArcsIn()) {
                if (arcSource.equals(arc.getSource())) {
                    break;
                }
                index++;
            }
            if (arcTarget instanceof Place) {
                writer.append("'" + arcTarget.getId() + "'.inTransition[" + index + "]");
            } else {
                writer.append("'" + arcTarget.getId() + "'.inPlaces[" + index + "]");
            }

            writer.append(")");
            //writer.append(" annotation(Line(color={0, 0, 0}, points={{0.0,0.0}, {0.0,0.0}}))");
            writer.append(";");
            writer.println();
        }
        writer.append(INDENT + "annotation(Icon(coordinateSystem(extent={{0.0,0.0},{0.0,0.0}})), Diagram(coordinateSystem(extent={{0.0,0.0},{0.0,0.0}})));");
        writer.println();

        writer.append("end '" + model.getName() + "'");
        writer.append(";");
        writer.println();

        writer.close();

        return file;
    }

    /**
     *
     * @param model
     * @param fileMOS
     * @param fileMO
     * @param workDirectory
     * @return map containing filter variables and the referenced elements
     * @throws IOException
     */
    public References exportMOS(PetriNet model, File fileMOS, File fileMO, File workDirectory) throws IOException {

        References references = new References();

        String filter = "variableFilter=\"";
        String tmp;
        int index;

        for (IElement arc : model.getArcs()) {
            arc.getFilter().clear();
        }

        for (INode place : model.getPlaces()) {

            place.getFilter().clear();

            tmp = "'" + place.getId() + "'.t";
            references.addElementReference(place, tmp);
            references.addFilterReference(tmp, place);
            filter += tmp + "|";

            index = 1;
            for (IArc arc : place.getArcsOut()) {

                tmp = "'" + arc.getSource().getId() + "'.tokenFlow.outflow[" + index + "]";
                references.addElementReference(place, tmp);
                references.addFilterReference(tmp, arc);
                filter += tmp + "|";

                tmp = "der(" + tmp + ")";
                references.addElementReference(place, tmp);
                references.addFilterReference(tmp, arc);
                filter += tmp + "|";

                index++;
            }

            index = 1;
            for (IArc arc : place.getArcsIn()) {

                tmp = "'" + arc.getTarget().getId() + "'.tokenFlow.inflow[" + index + "]";
                references.addElementReference(place, tmp);
                references.addFilterReference(tmp, arc);
                filter += tmp + "|";

                tmp = "der(" + tmp + ")";
                references.addElementReference(place, tmp);
                references.addFilterReference(tmp, arc);
                filter += tmp + "|";

                index++;
            }
        }

        for (INode transition : model.getTransitions()) {

            transition.getFilter().clear();

            String[] vars = new String[]{"fire", "actualSpeed"};
            for (String var : vars) {
                tmp = "'" + transition.getId() + "'." + var;
                references.addElementReference(transition, tmp);
                references.addFilterReference(tmp, transition);
                filter += tmp + "|";
            }
        }

        filter = filter.substring(0, filter.length() - 1);
        filter = filter.replace(".", "\\\\."); // might cause problems when custom names are used
        filter = filter.replace("[", "\\\\[");
        filter = filter.replace("]", "\\\\]");
        filter = filter.replace("(", "\\\\(");
        filter = filter.replace(")", "\\\\)");
        filter += "\"";

        FileWriter fstream = new FileWriter(fileMOS);
        BufferedWriter out = new BufferedWriter(fstream);

        out.write("cd(\"" + workDirectory.getPath().replace('\\', '/') + "/\"); ");
        out.write("getErrorString();\r\n");
        out.write("loadModel(" + properties.getProperty("openmodelica.library") + ");");
        out.write("getErrorString();\r\n");
        out.write("loadFile(\"" + fileMO.getPath().replace('\\', '/') + "\"); ");
        out.write("getErrorString();\r\n");
        out.write("setCommandLineOptions(\"--preOptModules+=unitChecking\");");
        out.write("getErrorString();\r\n");
        out.write("buildModel('" + model.getName() + "', " + filter + "); ");
        out.write("getErrorString();\r\n");

        out.close();

        return references;
    }

    /**
     * Closes a given Closable.
     *
     * @param stream
     */
    public void close(final Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
