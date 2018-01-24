/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.util;

import edu.unibi.agbi.petrinet.model.References;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Model;
import edu.unibi.agbi.petrinet.model.Parameter;
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
    private static final String CMNT_START = "/*";
    private static final String CMNT_END = "*/";

    private final String propertiesPath = "/openmodelica.properties";
    private final Properties properties;

    public OpenModelicaExporter() throws IOException {
        properties = new Properties();
        properties.load(OpenModelicaExporter.class.getResourceAsStream(propertiesPath));
    }

    public References export(String name, Model model, File fileMOS, File fileMO, File workDirectory, ParameterFactory parameterFactory) throws IOException {
        fileMO = exportMO(name, model, fileMO, parameterFactory);
        return exportMOS(name, model, fileMOS, fileMO, workDirectory);
    }

    /**
     *
     * @param name
     * @param model
     * @param file
     * @param parameterFactory
     * @return
     * @throws IOException
     */
    public File exportMO(String name, Model model, File file, ParameterFactory parameterFactory) throws IOException {

        final PrintWriter writer = new PrintWriter(file);

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
        writer.append(INDENT + "inner " + properties.getProperty("pnlib.settings"));
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
            transition.getLocalParameters().forEach(param -> {
                parameters.put("_" + transition.getId() + "_" + param.getId(), param);
            });
        }
        parameters.keySet().stream()
                .sorted((k1, k2) -> k1.toLowerCase().compareTo(k2.toLowerCase()))
                .filter(key -> parameters.get(key).getType() != Parameter.Type.REFERENCE)
                .forEach(key -> {
                    writer.append(INDENT + "parameter Real '" + key + "'");
                    if (parameters.get(key).getUnit() != null && !parameters.get(key).getUnit().isEmpty()) {
                        writer.append("(final unit=\"" + parameters.get(key).getUnit() + "\")");
                    }
                    writer.append(" = " + parameters.get(key).getValue() + ";");
                    writer.println();
                });

        /**
         * Places.
         */
        boolean isDisabled, isFirst;
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
            
            if (place.isDisabled()) {
                writer.append(CMNT_START);
            }
            
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
            writer.append("(nIn=" + place.getArcsIn().stream().filter(arc -> !arc.isDisabled()).count());
            writer.append(",nOut=" + place.getArcsOut().stream().filter(arc -> !arc.isDisabled()).count());

            isFirst = true;

            /**
             * Token
             */
            tmp1 = "";
            tmp2 = "";
            tmp3 = "";
            unit = "";
            
            for (Colour colour : colours) {

                token = place.getToken(colour);

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
                        throw new IOException("No token for colour '" + colour.getId() + "' available.");
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

            if (!isColoredPn && unit != null && !unit.isEmpty()) {
                writer.append(",t(final unit=\"" + unit + "\")");
            }
            
            writer.append(writePlaceConflictResolution(place));
            
            writer.append(")");
//            writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
            writer.append(";");
            
            if (place.isDisabled()) {
                writer.append(CMNT_END);
            }
            
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
            writer.append("(nIn=" + transition.getArcsIn().stream().filter(arc -> !arc.isDisabled()).count());
            writer.append(",nOut=" + transition.getArcsOut().stream().filter(arc -> !arc.isDisabled()).count());
            writer.append("," + functionType);

            // Function and parameters
            function = transition.getFunction();
            if (function.getUnit() != null && !function.getUnit().isEmpty()) {
                writer.append("(final unit=\"" + function.getUnit() + "\")");
            }
            if (transition.isDisabled()) {
                writer.append("=0" + CMNT_START + getFunctionValueString(parameterFactory, model, transition, function) + CMNT_END);
            } else {
                writer.append("=" + getFunctionValueString(parameterFactory, model, transition, function));
            }

            /**
             * Weights.
             */
            tmp1 = writeWeights(parameterFactory, model, transition.getArcsIn(), colours);
            if (!tmp1.isEmpty()) {
                writer.append(",arcWeightIn={" + tmp1 + "}");
            }
            tmp1 = writeWeights(parameterFactory, model, transition.getArcsOut(), colours);
            if (!tmp1.isEmpty()) {
                writer.append(",arcWeightOut={" + tmp1 + "}");
            }
            
            
            writer.append(")");
//            writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
            writer.append(";");
            writer.println();
        }

        /**
         * Arcs.
         */
        for (IArc arc : arcs) {
            
            if (arc.getArcType() == Arc.Type.NORMAL) {
                continue;
            }
            
            isDisabled = arc.isDisabled() || arc.getSource().isDisabled() || arc.getTarget().isDisabled();
            
            writer.append(INDENT);
            if (isDisabled) {
                writer.append(CMNT_START);
            }

            switch (arc.getArcType()) {

                case INHIBITORY:
                    writer.append(properties.getProperty("pnlib.arc.inhibitory"));
                    break;

                case TEST:
                    writer.append(properties.getProperty("pnlib.arc.test"));
                    break;

                default:
                    throw new IOException("Cannot export unhandled arc type: " + arc.getArcType());
            }
            
            writer.append(" '" + arc.getId() + "';");
            if (isDisabled) {
                writer.append(CMNT_END);
            }
            writer.println();
        }

        writer.append("equation");
        writer.println();

        /**
         * Connections.
         */
        for (IArc arc : arcs) {

            switch (arc.getArcType()) {
                case NORMAL:
                    writer.append(writeConnectionNormal(arc));
                    break;

                case INHIBITORY:
                    try {
                        writer.append(writeConnectionNonNormal(arc));
                    } catch (IOException ex) {
                        throw new IOException(ex.getMessage() + " Inhibitory arc cannot connect transition to place.");
                    }
                    break;

                case TEST:
                    try {
                        writer.append(writeConnectionNonNormal(arc));
                    } catch (IOException ex) {
                        throw new IOException(ex.getMessage() + " Text arc cannot connect transition to place.");
                    }
                    break;

                default:
                    throw new IOException("Unhandled connection for arc type '" + arc.getArcType() + "'.");
            }
            
            writer.println();
        }
//        writer.append(INDENT + "annotation(Icon(coordinateSystem(extent={{0.0,0.0},{0.0,0.0}})), Diagram(coordinateSystem(extent={{0.0,0.0},{0.0,0.0}})));");
//        writer.println();

        writer.append("end '" + name + "';");
        writer.println();

        writer.close();

        return file;
    }
    
    private String writePlaceConflictResolution(Place place) throws IOException {

        String confRes, confResValue;
        
        if (place.getArcsOut().size() <= 1) {
            return "";
        }

        switch (place.getConflictResolutionType()) {

            case PRIORITY:
                
                confRes = ", enablingType=PNlib.Types.EnablingType.Priority, enablingPrioOut={";
                
                for (int i = 0; i < place.getArcsOut().size(); i++) {
                    
                    confResValue = String.valueOf(i+1);
                    confRes += confResValue + ",";
                }
                
                confRes = confRes.substring(0, confRes.length() - 1);
                confRes += "}";
                
                break;

            case PROBABILITY:
                
                confRes = ", enablingType=PNlib.Types.EnablingType.Probability, enablingProbOut={0.5,0.5}";
                
                for (IArc arcOut : place.getArcsOut()) {
                    
//                    confResValue = String.valueOf(arcOut.getConflictResolutionValue() / place.getArcsOut().size());
//                    if (confResValue.length() > 5) {
//                        confResValue = confRes.substring(0, 5);
//                    }
                    confResValue = arcOut.getConflictResolutionValue() + "/" + place.getArcsOut().size();
                    confRes += confResValue + ",";
                }
                
                confRes = confRes.substring(0, confRes.length() - 1);
                confRes += "}";
                
                break;

            default:
                throw new IOException("Unhandled conflict resolution type detected!");
        }

        return confRes;
    }
    
    private String writeWeights(ParameterFactory parameterFactory, Model model, Collection<IArc> arcs, Collection<Colour> colours) throws IOException {
        
        boolean isFirstColour, isFirst = true;
        boolean isColoredPn = colours.size() != 1;
        String weight = "", tmp;

        for (IArc arc : arcs) {
            if (arc.isDisabled()) {
                continue;
            }
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
                    tmp += getWeightString(parameterFactory, model, arc, color);
                } else {
                    weight += getWeightString(parameterFactory, model, arc, color);
                }
            }
            if (isColoredPn) {
                weight += "{" + tmp + "}/*" + arc.getSource().getId() + "*/";
            }
        }
        return weight;
    }
    
    private String getWeightString(ParameterFactory parameterFactory, Model model, IArc arc, Colour colour) throws IOException {
        
        Weight weight = arc.getWeight(colour);
        Function function;
        
        if (weight != null) {
            function = weight.getFunction();
        } else {
            function = null;
        }
        
        if (arc.isDisabled() || function == null
                || arc.getSource().isConstant() || arc.getSource().isDisabled()
                || arc.getTarget().isConstant() || arc.getTarget().isDisabled()) {
            return "0" + CMNT_START + getFunctionValueString(parameterFactory, model, arc, function) + CMNT_END;
        } else {
            return getFunctionValueString(parameterFactory, model, arc, function);
        }
    }
    
    /**
     * Gets the string representing an element's function. 
     * 
     * @param parameterFactory
     * @param model the corresponding Model, will be used to get parameters
     * @param dataElement the corresponding Element, its ID will be added to specify local parameter names
     * @param function the Function to convert
     * @return
     * @throws IOException 
     */
    private String getFunctionValueString(ParameterFactory parameterFactory, Model model, IElement dataElement, Function function) throws IOException {
        
        String functionString = "";
        Parameter param;
        
        if (function != null) {

            for (Function functionElement : function.getElements()) {

                switch (functionElement.getType()) {

                    case FUNCTION:

                        functionString += getFunctionValueString(parameterFactory, model, dataElement, functionElement);
                        break;

                    case PARAMETER:

                        param = model.findParameter(parameterFactory, functionElement.getValue(), dataElement);
                        functionString += param.getValue();
                        break;

                    default:

                        functionString += functionElement.getValue();
                        break;
                }
            }
        }
        return functionString;
    }

    private String writeConnectionNormal(IArc arc) throws IOException {

        boolean isDisabled = arc.isDisabled() || arc.getSource().isDisabled() || arc.getTarget().isDisabled();
        String connection;

        connection = INDENT;
        if (isDisabled) {
            connection += CMNT_START;
        }
        connection += "connect(";
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
        if (isDisabled) {
            connection += CMNT_END;
        }

        return connection;
    }

    private String writeConnectionNonNormal(IArc arc) throws IOException {

        boolean isDisabled = arc.isDisabled() || arc.getSource().isDisabled() || arc.getTarget().isDisabled();
        String connection;

        if (arc.getSource() instanceof Place) {
            connection = INDENT;
            if (isDisabled) {
                connection += CMNT_START;
            }
            connection += "connect(";
            connection += "'" + arc.getSource().getId() + "'.outTransition[" + getArcIndexWithTargetNode(arc.getSource().getArcsOut(), arc.getTarget()) + "],";
            connection += "'" + arc.getId() + "'.inPlace";
            connection += ")";
//            connection +=" annotation(Line(color={0, 0, 0}, points={{0.0,0.0}, {0.0,0.0}}))";
            connection += ";";
            if (isDisabled) {
                connection += CMNT_END + "\n" + INDENT + CMNT_START;
            } else {
                connection += "\n" + INDENT;
            }
            connection += "connect(";
            connection += "'" + arc.getId() + "'.outTransition,";
            connection += "'" + arc.getTarget().getId() + "'.inPlaces[" + getArcIndexWithSourceNode(arc.getTarget().getArcsIn(), arc.getSource()) + "]";
            connection += ")";
//            connection +=" annotation(Line(color={0, 0, 0}, points={{0.0,0.0}, {0.0,0.0}}))";
            connection += ";";
            if (isDisabled) {
                connection += CMNT_END;
            }
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
            if (!place.isDisabled()) {
                setPlaceReferences(references, (Place) place);
            }
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

        filter = "'" + place.getId() + "'.t";
        references.addElementReference(place, filter);
        references.addFilterReference(filter, place);

        if (place.getPlaceType() == Place.Type.CONTINUOUS) {

            index = 1;
            for (IArc arc : place.getArcsOut()) {
                
                if (arc.isDisabled()) {
                    continue;
                }

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
                
                if (arc.isDisabled()) {
                    continue;
                }

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
            if (!a.isDisabled()) {
                index++; // no increment if disabled - handle as if arc does not exist
            }
        }
        throw new IOException("Node cannot be found to be source of the given arcs!");
    }

    private int getArcIndexWithTargetNode(Collection<IArc> arcs, INode target) throws IOException {
        int index = 1;
        for (IArc a : arcs) {
            if (target.equals(a.getTarget())) {
                return index;
            }
            if (!a.isDisabled()) {
                index++;
            }
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
