/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tutorial.grapheditor;

import java.util.List;
import java.util.Random;

/**
 * For sake of simplicity here's a simple 
 * layout algorithm in which random coordinates 
 * are used. Of course you'd have to do more 
 * complex stuff like tree layouts, etc.
 */
public class RandomLayout extends Layout {

    Graph graph;

    Random rnd = new Random();

    public RandomLayout(Graph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Cell> cells = graph.getModel().getAllCells();

        for (Cell cell : cells) {

            double x = rnd.nextDouble() * graph.getCanvas().getParent().getLayoutX();
            double y = rnd.nextDouble() * graph.getCanvas().getParent().getLayoutY();

            cell.relocate(x, y);

        }

    }

}
