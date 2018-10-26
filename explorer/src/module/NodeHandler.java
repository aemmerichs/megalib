package module;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.graph.Edge;
import org.eclipse.gef.graph.Graph;
import org.eclipse.gef.graph.Node;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnClickHandler;
import org.eclipse.gef.zest.fx.ZestProperties;
import org.eclipse.gef.zest.fx.parts.NodePart;

import javafx.scene.input.MouseEvent;
import explorer.parts.ExplorerController;

public class NodeHandler extends AbstractHandler implements IOnClickHandler {

	private Node current_main_node;
	
	@Override
	public void click(MouseEvent e) {
		double x = e.getSceneX();
		double y = e.getSceneY();
		//get NodePart of selected Node
		NodePart nPart = (NodePart) getHost();
		//get selected Node by using NodePart.getContent()
		Node n = nPart.getContent();
		//get Graph of Node
		Graph g = n.getGraph();
		if (e.isPrimaryButtonDown()) {
			goToLink(n);
		}
		if (e.isSecondaryButtonDown()) {
			//First of all reset all nodes to original color
			for(Node w: g.getNodes()){
				w.getAttributes().replace(ZestProperties.SHAPE_CSS_STYLE__N, w.getAttributes().get("original_shape_color"));
			}
			//if right click on already colored main node, undo previos coloring, by stopping here
			if (current_main_node != null && current_main_node.equals(n)){
				current_main_node = null;
				return;
			}
			//color selected node
			n.getAttributes().replace(ZestProperties.SHAPE_CSS_STYLE__N, "-fx-fill: #FFFF00; -fx-border-color:red");
			current_main_node = n;
			//iterative over edges and color neighbours of node n
			for (Edge r : g.getEdges()) {
				if (r.getSource().equals(n)) {
					r.getTarget().getAttributes().replace(ZestProperties.SHAPE_CSS_STYLE__N,
							"-fx-fill: #FFFFFF; -fx-border-color:red");
				} else if (r.getTarget().equals(n)) {
					r.getSource().getAttributes().replace(ZestProperties.SHAPE_CSS_STYLE__N,
							"-fx-fill: #FFFFFF; -fx-border-color:red");
				}
			}
			getHost().getRoot().refreshVisual();
		}
	}

	public static boolean openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean openWebpage(URL url) {
		try {
			return openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void goToLink(Node n) {
		LinkedList<String> x = (LinkedList<String>) n.getAttributes().get("alllinks");
		if (!x.isEmpty()) {
			URI link;
			for (String s : (LinkedList<String>) n.getAttributes().get("alllinks")) {
				try {
					link = new URI(s);
					openWebpage(link);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
