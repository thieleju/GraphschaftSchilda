package code;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;

import code.utils.BasicWindow;
import code.utils.GraphData;
import code.utils.GraphEdge;
import code.utils.GraphVertex;
import code.utils.JGraphPanel;

public class Problem3 extends BasicWindow {

  public Problem3(String title) throws FileNotFoundException {
    super(title);

    setSize(new Dimension(780, 480));
    setLayout(new GridLayout(1, 2));
    setLocationRelativeTo(null);

    GraphData graph_input = new GraphData("town_energy.json");

    // Erzeuge alle möglichen Kanten mit zufälligen Gewichten
    ArrayList<GraphEdge> random_weighted_edges = generate_all_edges(graph_input.getVertices());
    graph_input.setEdges(random_weighted_edges);

    int max_edges = 5;
    GraphData graph_output = kruskal(graph_input, max_edges);

    JGraphPanel p1 = new JGraphPanel("Rohdaten", graph_input, "circle");
    JGraphPanel p2 = new JGraphPanel("MST (Kruskal, max " + max_edges + " edges)", graph_output, "circle");

    add(p1);
    add(p2);
  }

  private GraphData kruskal(GraphData input, int max_edges) {

    // Lese die Knoten und Kanten aus den Rohdaten
    ArrayList<GraphVertex> vertices = input.getVertices();
    ArrayList<GraphEdge> edges = input.getEdges();

    // Sortiere die Kanten nach Gewicht
    edges.sort(Comparator.comparingDouble(GraphEdge::getWeight));

    // erstelle einen wald 'forest' (eine menge von bäumen), wo jeder knoten ein
    // eigener baum ist
    ArrayList<ArrayList<GraphVertex>> forest = new ArrayList<ArrayList<GraphVertex>>();
    for (GraphVertex v : vertices) {
      ArrayList<GraphVertex> tree = new ArrayList<GraphVertex>();
      tree.add(v);
      forest.add(tree);
    }

    // erstelle eine liste mit den kanten des minimum spanning trees
    ArrayList<GraphEdge> forest_edges = new ArrayList<GraphEdge>(edges);

    // erstelle eine liste für die Ausgabe
    ArrayList<GraphEdge> output_edges = new ArrayList<GraphEdge>();

    // solange der wald nicht leer ist und der baum noch nicht alle knoten enthält
    while (forest_edges.size() > 0) {
      // entferne eine kante (u, v) aus forest
      GraphEdge e = forest_edges.remove(0);

      // finde die bäume, die mit der Kante e verbunden sind
      ArrayList<GraphVertex> tree_u = null;
      ArrayList<GraphVertex> tree_v = null;
      for (ArrayList<GraphVertex> t : forest) {
        if (t.contains(GraphData.getSourceVertexFromEdge(e, vertices)))
          tree_u = t;
        if (t.contains(GraphData.getTargetVertexFromEdge(e, vertices)))
          tree_v = t;
      }

      // Prüfe ob die kante e von einem vertex ausgeht, der bereits mehr als 5 kanten
      // hat
      ArrayList<GraphEdge> source_edges = GraphData.getAdjacentEdges(e.getSource(), output_edges);
      ArrayList<GraphEdge> target_edges = GraphData.getAdjacentEdges(e.getTarget(), output_edges);

      if (source_edges.size() >= max_edges || target_edges.size() >= max_edges)
        continue;

      // wenn u und v in gleichen Bäumen sind -> skip
      if (tree_u == tree_v)
        continue;

      // füge kante von u und v zur Ausgabe hinzu
      output_edges.add(e);

      // füge baum von v zu baum von u hinzu (merge)
      for (GraphVertex v : tree_v)
        tree_u.add(v);

      forest.remove(tree_v);
    }

    return new GraphData(vertices, output_edges, false);
  }

  private ArrayList<GraphEdge> generate_all_edges(ArrayList<GraphVertex> vertices) {
    ArrayList<GraphEdge> output = new ArrayList<GraphEdge>();
    for (int i = 0; i < vertices.size(); i++) {
      for (int j = i + 1; j < vertices.size(); j++) {
        // generate random weight and add new edge to output
        output.add(new GraphEdge(vertices.get(i).getLabel(),
            vertices.get(j).getLabel(), Math.round(Math.random() * 100.0)));
      }
    }
    return output;
  }
}