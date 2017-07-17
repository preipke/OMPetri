/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.util;

import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Parameter;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    public References export(String name, Model model, File fileMOS, File fileMO, File workDirectory) throws IOException {
        fileMO = exportMO(name, model, fileMO);
        return exportMOS(name, model, fileMOS, fileMO, workDirectory);
    }

    /**
     *
     * @param name
     * @param model
     * @param file
     * @return
     * @throws IOException
     */
    public File exportMO(String name, Model model, File file) throws IOException {

        PrintWriter writer = new PrintWriter(file);

        Collection<Arc> arcs = model.getArcs();
        Collection<Colour> colours = model.getColours();
        Collection<Place> places = model.getPlaces();
        Collection<Transition> transitions = model.getTransitions();
        Map<String,Parameter> parameters = new HashMap();

        boolean isColoredPn = colours.size() != 1;

        /**
         * Model name.
         */
        writer.append("model '" + name + "'");
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
         * Parameters.
         */
        model.getParameters().forEach(param -> {
            parameters.put("_" + param.getId(), param);
        });
        for (Transition transition : transitions) {
            transition.getParameters().forEach(param -> {
                parameters.put("_" + transition.getId() + "_" + param.getId(), param);
            });
        }
        parameters.keySet().stream()
                .sorted((k1, k2) -> k1.toLowerCase().compareTo(k2.toLowerCase()))
                .filter(key -> parameters.get(key).getType() != Parameter.Type.REFERENCE)
                .forEach(key -> {
                    writer.append(INDENT + "parameter Real '" + key + "'");
//                    writer.append("(final unit=\"" + parameters.get(key).getUnit() + "\")");
                    writer.append(" = " + parameters.get(key).getValue() + ";");
                    writer.println();
                });

        /**
         * Places.
         */
        boolean isFirst;
        String functionType, tokenType, tmp1, tmp2, tmp3, unit;
        Function function;
        Token token;
        
        Place place;
        Iterator<Place> itPlaces
                = places.stream()
                        .sorted((p1, p2) -> p1.getId().toLowerCase().compareTo(p2.getId().toLowerCase()))
                        .iterator();

        Transition transition;
        Iterator<Transition> itTransitions
                = transitions.stream()
                        .sorted((p1, p2) -> p1.getId().toLowerCase().compareTo(p2.getId().toLowerCase()))
                        .iterator();

        while (itPlaces.hasNext()) {
            place = itPlaces.next();

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
                    throw new IOException("Unhandled place type: " + place.getPlaceType());
            }
            writer.append(" '" + place.getId() + "'");
            writer.append("(nIn=" + place.getArcsIn().size());
            writer.append(",nOut=" + place.getArcsOut().size());

            isFirst = true;

            /**
             * Token
             */
            tmp1 = "";
            tmp2 = "";
            tmp3 = "";
            unit = "";
            
            for (Colour color : colours) {

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

                    if (token == null) {
                        throw new IOException("No token for default colour '" + color.getId() + "' available.");
                    }
                    
                    if (place.getPlaceType() == Place.Type.CONTINUOUS) {
                        tmp1 += token.getValueStart();
                        tmp2 += token.getValueMin();
                        tmp3 += token.getValueMax();
                    } else {
                        tmp1 += (int) token.getValueStart();
                        tmp2 += (int) token.getValueMin();
                        tmp3 += (int) token.getValueMax();
                    }
                    unit = token.getUnit();
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

            if (!isColoredPn) {
//                writer.append(",t(final unit=\"" + unit + "\")");
            }
            writer.append(")");
//            writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
            writer.append(";");
            writer.println();
        }

        /**
         * Transitions.
         */
        while (itTransitions.hasNext()) {
            transition = itTransitions.next();

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
                    throw new IOException("Unhandled transition type: " + transition.getTransitionType());
            }
            writer.append(" '" + transition.getId() + "'");
            writer.append("(nIn=" + transition.getArcsIn().size());
            writer.append(",nOut=" + transition.getArcsOut().size());
            writer.append("," + functionType);

            // Function and parameters
            function = transition.getFunction();
            if (function.getUnit() != null && !function.getUnit().matches("")) {
                writer.append("(final unit=\"" + function.getUnit() + "\")");
            }
            if (transition.isDisabled()) {
                writer.append("=0/*" + getFunctionString(model, transition, function) + "*/");
            } else {
                writer.append("=" + getFunctionString(model, transition, function));
            }

            /**
             * Weights.
             */
            writer.append(",arcWeightIn={" + getWeightString(transition.getArcsIn(), colours) + "}");
            writer.append(",arcWeightOut={" + getWeightString(transition.getArcsOut(), colours) + "}");
            
            writer.append(")");
//            writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
            writer.append(";");
            writer.println();
        }

        /**
         * Arcs.
         */
        for (IArc arc : arcs) {

            switch (arc.getArcType()) {

                case INHIBITORY:
                    writer.append(INDENT);
                    writer.append(properties.getProperty("pnlib.arc.inhibitory"));
                    writer.append(" '" + arc.getId() + "';");
                    writer.println();
                    break;

                case NORMAL:
                    break;

                case READ:
                    writer.append(INDENT);
                    writer.append(properties.getProperty("pnlib.arc.read"));
                    writer.append(" '" + arc.getId() + "';");
                    writer.println();
                    break;

                case TEST:
                    writer.append(INDENT);
                    writer.append(properties.getProperty("pnlib.arc.test"));
                    writer.append(" '" + arc.getId() + "';");
                    writer.println();
                    break;

                default:
                    throw new IOException("Cannot export unhandled arc type: " + arc.getArcType());
            }
        }

        writer.append("equation");
        writer.println();

        /**
         * Connections.
         */
        for (IArc arc : arcs) {

            switch (arc.getArcType()) {
                case NORMAL:
                    writer.append(getConnectionNormal(arc));
                    break;

                case INHIBITORY:
                    try {
                        writer.append(getConnectionNonNormal(arc));
                    } catch (IOException ex) {
                        throw new IOException(ex.getMessage() + " Inhibitory arc cannot connect transition to place.");
                    }
                    break;

                case TEST:
                    try {
                        writer.append(getConnectionNonNormal(arc));
                    } catch (IOException ex) {
                        throw new IOException(ex.getMessage() + " Text arc cannot connect transition to place.");
                    }
                    break;

                case READ:
                    try {
                        writer.append(getConnectionNonNormal(arc));
                    } catch (IOException ex) {
                        throw new IOException(ex.getMessage() + " Read arc cannot connect transition to place.");
                    }
                    break;

                default:
                    throw new IOException("Unhandled connection for arc type '" + arc.getArcType() + "'.");
            }
            writer.println();
        }
        
        writer.append(INDENT + "annotation(Icon(coordinateSystem(extent={{0.0,0.0},{0.0,0.0}})), Diagram(coordinateSystem(extent={{0.0,0.0},{0.0,0.0}})));");
        writer.println();

        writer.append("end '" + name + "';");
        writer.println();

        writer.close();

        return file;
    }
    
    private String getWeightString(Collection<IArc> arcs, Collection<Colour> colours) {
        
        boolean isFirstColour, isFirst = true;
        boolean isColoredPn = colours.size() != 1;
        String weight = "", tmp;

        for (IArc arc : arcs) {
            if (isFirst) {
                isFirst = false;
            } else {
                weight += ",";
            }
            tmp = "";
            isFirstColour = true;
            for (Colour color : colours) {
                if (isFirstColour) {
                    isFirstColour = false;
                } else {
                    tmp += ",";
                }
                if (isColoredPn) {
                    tmp += getWeightString(arc, color);
                } else {
                    weight += getWeightString(arc, color);
                }
            }
            if (isColoredPn) {
                weight += "{" + tmp + "}/*" + arc.getSource().getId() + "*/";
            }
        }
        return weight;
    }
    
    private String getWeightString(IArc arc, Colour colour) {
        Weight weight = arc.getWeight(colour);
        if (arc.isDisabled() || arc.getSource().isDisabled() || arc.getTarget().isDisabled() || weight == null) {
            return "0";
        } else {
            return weight.getValue();
        }
    }
    
    /**
     * Gets the string representing a transition's function. In creation, local
     * parameters will be prioritized over global parameters.
     * 
     * @param model
     * @param function
     * @return 
     */
    private String getFunctionString(Model model, Transition transition, Function function) {
        String functionString = "";
        Parameter parameter;
        for (Function element : function.getElements()) {
            switch (element.getType()) {
                case FUNCTION:
                    functionString += getFunctionString(model, transition, element);
                    break;
                case PARAMETER:
                    if ((parameter = model.getParameter(element.getValue())) != null) {
                        if (parameter.getType() == Parameter.Type.REFERENCE) {
                            functionString += parameter.getValue();
                        } else {
                            functionString += "'_" + parameter.getId() + "'";
                        }
                    } else {
                        functionString += "'_" + transition.getId() + "_" + element.getValue() + "'";
                    }
                    break;
                default:
                    functionString += element.getValue();
                    break;
            }
        }
        return functionString;
    }

    private String getConnectionNormal(IArc arc) throws IOException {

        String connection;

        connection = INDENT + "connect(";
        if (arc.getSource() instanceof Place) {
            connection += "'" + arc.getSource().getId() + "'.outTransition[" + getArcIndexWithTargetNode(arc.getSource().getArcsOut(), arc.getTarget()) + "],";
            connection += "'" + arc.getTarget().getId() + "'.inPlaces[" + getArcIndexWithSourceNode(arc.getTarget().getArcsIn(), arc.getSource()) + "]";
        } else {
            connection += "'" + arc.getSource().getId() + "'.outPlaces[" + getArcIndexWithTargetNode(arc.getSource().getArcsOut(), arc.getTarget()) + "],";
            connection += "'" + arc.getTarget().getId() + "'.inTransition[" + getArcIndexWithSourceNode(arc.getTarget().getArcsIn(), arc.getSource()) + "]";
        }
        connection += ")";
//        connection +=" annotation(Line(color={0, 0, 0}, points={{0.0,0.0}, {0.0,0.0}}))";
        connection += ";";

        return connection;
    }

    private String getConnectionNonNormal(IArc arc) throws IOException {

        String connection;

        if (arc.getSource() instanceof Place) {
            connection = INDENT + "connect(";
            connection += "'" + arc.getSource().getId() + "'.outTransition[" + getArcIndexWithTargetNode(arc.getSource().getArcsOut(), arc.getTarget()) + "],";
            connection += "'" + arc.getId() + "'.inPlace";
            connection += ")";
//            connection +=" annotation(Line(color={0, 0, 0}, points={{0.0,0.0}, {0.0,0.0}}))";
            connection += ";\n";
            connection += INDENT + "connect(";
            connection += "'" + arc.getId() + "'.outTransition,";
            connection += "'" + arc.getTarget().getId() + "'.inPlaces[" + getArcIndexWithSourceNode(arc.getTarget().getArcsIn(), arc.getSource()) + "]";
            connection += ")";
//            connection +=" annotation(Line(color={0, 0, 0}, points={{0.0,0.0}, {0.0,0.0}}))";
            connection += ";";
        } else {
            throw new IOException("Invalid connection found!");
        }

        return connection;
    }

    /**
     *
     * @param name
     * @param model
     * @param fileMOS
     * @param fileMO
     * @param workDirectory
     * @return map containing filter variables and the referenced elements
     * @throws IOException
     */
    public References exportMOS(String name, Model model, File fileMOS, File fileMO, File workDirectory) throws IOException {

        References references = getModelReferences(model);
        Collection<String> filters = references.getFilterToElementReferences().keySet();

        String allFilter = "variableFilter=\"";
        for (String filter : filters) {
            allFilter += filter + "|";
        }

        allFilter = allFilter.substring(0, allFilter.length() - 1);
        allFilter = allFilter.replace(".", "\\\\."); // might cause problems when custom names are used
        allFilter = allFilter.replace("[", "\\\\[");
        allFilter = allFilter.replace("]", "\\\\]");
        allFilter = allFilter.replace("(", "\\\\(");
        allFilter = allFilter.replace(")", "\\\\)");
        allFilter += "\"";

        FileWriter fstream = new FileWriter(fileMOS);
        BufferedWriter out = new BufferedWriter(fstream);

        out.write("cd(\"" + workDirectory.getPath().replace('\\', '/') + "/\"); getErrorString();\r\n");
        out.write("loadModel(" + properties.getProperty("openmodelica.library") + "); getErrorString();\r\n");
        out.write("loadFile(\"" + fileMO.getPath().replace('\\', '/') + "\"); getErrorString();\r\n");
        out.write("setCommandLineOptions(\"--preOptModules+=unitChecking\"); getErrorString();\r\n");
        out.write("buildModel('" + name + "', " + allFilter + "); getErrorString();\r\n");
//        out.write("buildModel('" + name + "'); getErrorString();\r\n");

        out.close();

        return references;
    }

    /**
     * Creates and gets element filter references for an entire model.
     *
     * @param model
     * @return
     * @throws IOException
     */
    public References getModelReferences(Model model) throws IOException {
        References references = new References();
        for (INode place : model.getPlaces()) {
            setPlaceReferences(references, (Place) place);
        }
        for (INode transition : model.getTransitions()) {
            setTransitionReferences(references, (Transition) transition);
        }
        return references;
    }

    /**
     * Sets element filter references for a place.
     *
     * @param references
     * @param place
     * @return
     * @throws IOException
     */
    private References setPlaceReferences(References references, Place place) throws IOException {

        Transition transition;
        String filter;
        int index;

        // uncomment if making public
//        if (references.getElementToFilterReferences().containsKey(place)) {
//            references.getElementToFilterReferences().get(place).clear();
//        }
        filter = "'" + place.getId() + "'.t";
        references.addElementReference(place, filter);
        references.addFilterReference(filter, place);

        if (place.getPlaceType() == Place.Type.CONTINUOUS) {

            index = 1;
            for (IArc arc : place.getArcsOut()) {

                transition = (Transition) arc.getTarget();
                if (transition.getTransitionType() == Transition.Type.CONTINUOUS) {

                    filter = "'" + arc.getSource().getId() + "'.tokenFlow.outflow[" + index + "]";
                    references.addElementReference(arc, filter);
                    references.addElementReference(place, filter);
                    references.addFilterReference(filter, place);

                    filter = "der(" + filter + ")";
                    references.addElementReference(arc, filter);
                    references.addElementReference(place, filter);
                    references.addFilterReference(filter, place);
                }
                index++;
            }

            index = 1;
            for (IArc arc : place.getArcsIn()) {

                transition = (Transition) arc.getSource();
                if (transition.getTransitionType() == Transition.Type.CONTINUOUS) {

                    filter = "'" + arc.getTarget().getId() + "'.tokenFlow.inflow[" + index + "]";
                    references.addElementReference(arc, filter);
                    references.addElementReference(place, filter);
                    references.addFilterReference(filter, place);

                    filter = "der(" + filter + ")";
                    references.addElementReference(arc, filter);
                    references.addElementReference(place, filter);
                    references.addFilterReference(filter, place);
                }
                index++;
            }
        }

        return references;
    }

    /**
     * Sets element filter references for a transition.
     *
     * @param references
     * @param transition
     * @return
     * @throws IOException
     */
    private References setTransitionReferences(References references, Transition transition) throws IOException {

        String filter;

        if (transition.getTransitionType() == Transition.Type.CONTINUOUS) {
            filter = "'" + transition.getId() + "'.actualSpeed";
            references.addElementReference(transition, filter);
            references.addFilterReference(filter, transition);
        }

        filter = "'" + transition.getId() + "'.fire";
        references.addElementReference(transition, filter);
        references.addFilterReference(filter, transition);

        return references;
    }

    private int getArcIndexWithSourceNode(Collection<IArc> arcs, INode source) throws IOException {
        int index = 1;
        for (IArc a : arcs) {
            if (source.equals(a.getSource())) {
                return index;
            }
            index++;
        }
        throw new IOException("Node cannot be found to be source of the given arcs!");
    }

    private int getArcIndexWithTargetNode(Collection<IArc> arcs, INode target) throws IOException {
        int index = 1;
        for (IArc a : arcs) {
            if (target.equals(a.getTarget())) {
                return index;
            }
            index++;
        }
        throw new IOException("Node cannot be found to be target of the given arcs!");
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
