package org.softlang.java.parts;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.gef.graph.Edge;
import org.eclipse.gef.graph.Graph;
import org.eclipse.gef.graph.Node;
import org.eclipse.gef.zest.fx.ZestFxModule;
import org.eclipse.gef.zest.fx.ZestProperties;
import org.eclipse.gef.zest.fx.ui.ZestFxUiModule;
import org.eclipse.gef.zest.fx.ui.parts.ZestFxUiView;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Guice;
import com.google.inject.util.Modules;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;


@SuppressWarnings("restriction")
public class GraphView extends ZestFxUiView implements IShowInTarget {

	String fname;
	WatchService w;
	WatchKey k;
	Path path;
	String propertiesPath;
	Boolean watchServiceRunning = false;
	String LABEL = ZestProperties.LABEL__NE;
	LinkedList<Node> nodeList = new LinkedList<Node>();
	Map<String,String> colour = new HashMap<String,String>();

	public GraphView() {
		super(Guice.createInjector(Modules.override(new ZestFxUiModule()).with(new ZestFxUiCustom())));
	}
	
	@Override
	public boolean show(ShowInContext cxt) {
		
		Object in = cxt.getInput();
		if (in instanceof File) {
			IEclipsePreferences preferences = ConfigurationScope.INSTANCE
				    .getNode("org.softlang.java");
			String dotExecutablePath = preferences.get("Graphvizz", "null");
			
			if(dotExecutablePath.endsWith("dot.exe") || dotExecutablePath.endsWith("dot")) {
			File f = (File) in;
			fname = f.getName();
			setWatchService(f);
			Graph g = createGraphFromJson(f.toPath());
			setGraph(g);
			}
			else {
				nodeList = new LinkedList();
				nodeList.add(createNode("Please enter a valid path to your \n"
						+ "dot.exe (Windows) or dot-file (Linux/Mac) under: \n"
						+ "Window>Preferences>MegaLPreference", new LinkedList(), new LinkedList()));
				Graph.Builder gbuilder = new Graph.Builder();
				Graph g = gbuilder.nodes(nodeList).build();
				setGraph(g);
			}
			return true;
		}
		return false;
	}

	public void setWatchService(File f) {
		try {
			w = FileSystems.getDefault().newWatchService();
			path = Paths.get(f.getParent());
			k = path.register(w, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
			if (!watchServiceRunning) {
				createWatchService(f);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createWatchService(File f) {
		ScheduledService<Graph> s = new ScheduledService() {
			@Override
			protected Task<Graph> createTask() {
				Task<Graph> task = new Task<Graph>() {
					@Override
					protected Graph call() throws Exception {
						WatchKey keyTemp;
						while ((keyTemp = w.take()) != null) {
							for (WatchEvent<?> event : k.pollEvents()) {
								if (event.context().toString().equals(fname)) {
									Path completeFilePath = Paths.get(path.toString(),event.context().toString());
									k.reset();
									return createGraphFromJson(completeFilePath);
								}
							}
							k.reset();
						}
						return null;
					}
				};
				return task;
		};
		};
		s.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				setGraph(s.getValue());
			}
		});
		s.start();
	}

	private Graph createGraphFromJson(Path f) {	
		String json;
		nodeList = new LinkedList();
		try {
			LinkedList<Edge> edgeList = new LinkedList<Edge>();
			LinkedList<String> urls = null;
			LinkedList<String> bindings = null;
			
			json = FileUtils.readFileToString(f.toFile());
			JsonElement root = new JsonParser().parse(json);
			JsonArray nodes =  (JsonArray) root.getAsJsonObject().get("Nodes");
			JsonArray edges = (JsonArray) root.getAsJsonObject().get("Edges");
			
			IEclipsePreferences preferences = ConfigurationScope.INSTANCE
				    .getNode("org.softlang.java");
			propertiesPath = preferences.get("properties", "null");
			
			for(JsonElement i:edges) {
				JsonObject edge = i.getAsJsonObject();
				if(edge.get("label").getAsString().equals("subtypeOf")) {
					colour.put(edge.get("source").getAsString(), edge.get("target").getAsString());
					}
				}
			
			for(JsonElement j: nodes) {
				String name = j.getAsJsonObject().get("name").getAsString();
				if(j.getAsJsonObject().has("links")) {
					urls = new LinkedList<String>();
					for(JsonElement u: j.getAsJsonObject().get("links").getAsJsonArray()) {
					urls.add(u.getAsString());
					}
				}
				if(j.getAsJsonObject().has("bindings")) {
					bindings = new LinkedList<String>();
					for(JsonElement b: j.getAsJsonObject().get("bindings").getAsJsonArray()) {
					bindings.add(b.getAsString());
					}
				}
				nodeList.add(createNode(name, urls, bindings));						
			}
			
			for(JsonElement i:edges) {
				JsonObject edge = i.getAsJsonObject();
				edgeList.add(createEdge(edge.get("label").getAsString(), edge.get("source").getAsString(), edge.get("target").getAsString()));
			}
			
			Graph.Builder gbuilder = new Graph.Builder();
			return gbuilder.nodes(nodeList).edges(edgeList).attr(ZestProperties.LAYOUT_ALGORITHM__G, new CustomDotLayout()).build();
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return null;
	}

	private Edge createEdge(String label, String source, String target){
		if(!(checkifNodeinGraph(source))) {
			nodeList.add(createNode(source,new LinkedList(),new LinkedList()));
		}
		if(!(checkifNodeinGraph(target))) {
			nodeList.add(createNode(target,new LinkedList(),new LinkedList()));
		}
		Node nOrigin = null;
		Node nDestination = null;
		for(Node n:nodeList) {
			if(n.getAttributes().get(LABEL).equals(source)) {
				nOrigin = n;
			}
			if(n.getAttributes().get(LABEL).equals(target)) {
				nDestination = n;
			}
		}
		Edge.Builder builder = new org.eclipse.gef.graph.Edge.Builder(nOrigin, nDestination).attr(LABEL, label);
        builder.attr(ZestProperties.TARGET_DECORATION__E,new Triangle());
		return builder.buildEdge();
	}
	
	private Node createNode(String name, LinkedList<String> urls, LinkedList<String> bindings){
		Node.Builder builder = new Node.Builder();
		builder.attr(LABEL, name);
		builder.attr("alllinks", urls);
		builder.attr("bindings", bindings);
		builder.attr(ZestProperties.INVISIBLE__NE, false);			
		builder.attr("original_shape_color", "-fx-fill:" + lookupcolour(name));
		builder.attr(ZestProperties.SHAPE_CSS_STYLE__N, "-fx-fill: " + lookupcolour(name));
		boolean haslink = false;
		if(!urls.isEmpty()) {
			haslink = true;
		}
		builder.attr(ZestProperties.LABEL_CSS_STYLE__NE,"-fx-fill: #000000;-fx-underline:" + haslink); 
		Node node = builder.buildNode();
		return node;
	}
	
	private String lookupcolour (String key ) {
		File f = new File(propertiesPath);
		String json;
		try {
			json = FileUtils.readFileToString(f);
			JsonElement root = new JsonParser().parse(json);
			if(root.getAsJsonObject().has(key)) {
				return root.getAsJsonObject().get(key).getAsString();
			}
			while(colour.get(key) != null){
				key = colour.get(key);
				if(root.getAsJsonObject().has(key)) {
					return root.getAsJsonObject().get(key).getAsString();
				}
			}
		} catch (Exception e) {
			return "#FFFFFF";
		}
		return "#FFFFFF";
	}
	
	private boolean checkifNodeinGraph(String name) {
			for(Node n:nodeList) {
				if(n.getAttributes().get(LABEL).equals(name)) {
					return true;
				}
			}
		return false;
	}
	
}