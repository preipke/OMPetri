/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.util;

import edu.unibi.agbi.petrinet.entity.PN_Node;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.PetriNet;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author PR
 */
public class OM_Exporter
{
    public OM_Exporter() {
        
    }
    
    public void export(PetriNet petriNet, File file) {
        
        Collection<Arc> arcs = petriNet.getArcs();
        Collection<Colour> colors = petriNet.getColours();
        Collection<Place> places = petriNet.getPlaces();
        Collection<Transition> transitions = petriNet.getTransitions();
        
        boolean isCpn = colors.size() != 1;
        String pnLibPlace, pnLibTransition;
        
        if (isCpn) {
            pnLibPlace = "PNlib.Examples.Models.ColoredPlaces.CPC";
            pnLibTransition = "PNlib.Examples.Models.ColoredPlaces.CTC";
        } else {
            pnLibPlace = "PNlib.PC";
            pnLibTransition = "PNlib.TC";
        }
        
        
        try (PrintWriter writer = new PrintWriter(file)) {

            writer.append("model '" + petriNet.getName() + "'");
            writer.println();

            /**
             * Settings.
             */
            
            writer.append("  ");
            writer.append(" inner PNlib.Settings");
            writer.append(" settings(showTokenFlow = true)");
            //writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
            writer.append(";");
            writer.println();

            /**
             * Places.
             */
            
            boolean isFirst, isInnerFirst;
            String tmp1, tmp2, tmp3;
            Function function;
            Map<Colour,Token> tokenMap;
            Map<Colour,Weight> weightMap;
            Token token;
            Weight weight;
            PN_Node node;
            int count;
            
            for (Place place : places) {

                tokenMap = place.getTokenMap();
                
                tmp1 = "";
                tmp2 = "";
                tmp3 = "";
                
                writer.append("  ");
                writer.append(pnLibPlace);
                writer.append(" '" + place.getId() + "'");
                writer.append("(nIn=" + place.getArcsIn().size());
                writer.append(",nOut=" + place.getArcsOut().size());

                isFirst = true;
                
                /**
                 * Token
                 */

                for (Colour color : colors) {
                    
                    token = tokenMap.get(color);

                    if (isCpn) {

                        if (isFirst) {

                            isFirst = false;

                        } else {

                            tmp1 += ",";
                            tmp2 += ",";
                            tmp3 += ",";
                        }

                        if (token != null) {
                            
                            tmp1 += token.getValueStart();
                            tmp2 += token.getValueMin();
                            tmp3 += token.getValueMax();
                            
                        } else {
                            
                            tmp1 += "0";
                            tmp2 += "0";
                            tmp3 += "0";
                            
                        }
                        
                    } else {
                        
                        tmp1 += token.getValueStart();
                        tmp2 += token.getValueMin();
                        tmp3 += token.getValueMax();
                    }

                }
                
                writer.append(",startMarks={" + tmp1 + "}");
                writer.append(",minMarks={" + tmp2 + "}");
                writer.append(",maxMarks={" + tmp3 + "}");
                //writer.append(",t(final unit=\"custom unit\")");
                writer.append(")");
                //writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
                writer.append(";");
                writer.println();
            }

            /**
             * Transitions.
             */
            
            for (Transition transition : transitions) {
                
                function = transition.getFunction();
                
                writer.append("");
                writer.append(pnLibTransition);
                writer.append(" '" + transition.getId() + "'");
                writer.append("(nIn=" + transition.getArcsIn().size());
                writer.append(",nOut=" + transition.getArcsOut().size());
                writer.append(",maximumSpeed(final unit=\"" + function.getUnit() + "\")=" + function.toString());
                
                /**
                 * Weights, incoming
                 */
                
                tmp1 = "";
                isFirst = true;
                
                for (Arc arc : transition.getArcsIn()) {

                    weightMap = arc.getWeightMap();

                    if (isFirst) {

                        isFirst = false;

                    } else {

                        tmp1 += ",";

                    }

                    tmp3 = "";
                    isInnerFirst = true;
                    
                    for (Colour color : colors) {
                        
                        weight = weightMap.get(color);
                        
                        if (isInnerFirst) {
                            
                            isInnerFirst = false;
                            
                        } else {
                            
                            tmp3 += ",";
                            
                        }

                        if (isCpn) {
                            
                            if (weight != null) {
                                
                               tmp3 += weight.getValue();
                                
                            } else {
                                
                               tmp3 += "0";
                                
                            }

                        } else {

                            tmp1 += weight.getValue();
                        
                        }
                    
                    }
                    
                    if (isCpn) {

                        tmp1 += "{" + tmp3 + "}/*" + arc.getSource().getId() + "*/";
                        
                    }
                    
                }
                
                /**
                 * Weights, outgoing
                 */
                
                tmp2 = "";
                isFirst = true;
                
                for (Arc arc : transition.getArcsOut()) {

                    weightMap = arc.getWeightMap();

                    if (isFirst) {

                        isFirst = false;

                    } else {

                        tmp2 += ",";

                    }

                    tmp3 = "";
                    isInnerFirst = true;
                    
                    for (Colour color : colors) {
                        
                        weight = weightMap.get(color);
                        
                        if (isInnerFirst) {
                            
                            isInnerFirst = false;
                            
                        } else {
                            
                            tmp3 += ",";
                            
                        }

                        if (isCpn) {
                            
                            if (weight != null) {
                                
                               tmp3 += weight.getValue();
                                
                            } else {
                                
                               tmp3 += "0";
                                
                            }

                        } else {

                            tmp2 += weight.getValue();
                        
                        }
                    
                    }
                    
                    if (isCpn) {

                        tmp2 += "{" + tmp3 + "}/*" + arc.getSource().getId() + "*/";
                        
                    }
                    
                }

                writer.append(",arcWeightIn={" + tmp1 + "}");
                writer.append(",arcWeightOut={" + tmp2 + "}");
                writer.append(")");
                //writer.append(" annotation(Placement(visible=true, transformation(origin={0.0,0.0}, extent={{0,0}, {0,0}}, rotation=0)))");
                writer.append(";");
                writer.println();
            }

            /**
             * Arcs.
             */
            
            writer.append("equation");
            writer.println();
            
            for (Arc arc : arcs) {
                
                writer.append("  ");
                writer.append("connect(");
                
                node = arc.getSource();
                count = 1;
                
                for (Arc nodeArc : node.getArcsOut()) {
                    
                    if (arc.equals(nodeArc)) {
                        break;
                    }
                    
                    count++;
                
                }
                
                if (node instanceof Place) {
                    writer.append("'" + node.getId() + "'.outPlaces[" + count + "]");
                } else {
                    writer.append("'" + node.getId() + "'.outTransition[" + count + "]");
                }
                
                node = arc.getTarget();
                count = 1;
                
                for (Arc nodeArc : node.getArcsOut()) {
                    
                    if (arc.equals(nodeArc)) {
                        break;
                    }
                    
                    count++;
                
                }
                
                if (node instanceof Place) {
                    writer.append("'" + node.getId() + "'.inPlaces[" + count + "]");
                } else {
                    writer.append("'" + node.getId() + "'.inTransition[" + count + "]");
                }
                
                writer.append(")");
                //writer.append(" annotation(Line(color={0, 0, 0}, points={{0.0,0.0}, {0.0,0.0}}))");
                writer.append(";");
                writer.println();
            }
            writer.append("  annotation(Icon(coordinateSystem(extent={{0.0,0.0},{0.0,0.0}})), Diagram(coordinateSystem(extent={{0.0,0.0},{0.0,0.0}})));");
            
            writer.append("end '" + petriNet.getName() + "'");
            writer.append(";");
            writer.println();

        } catch (IOException ex) {

        }
    }
}
