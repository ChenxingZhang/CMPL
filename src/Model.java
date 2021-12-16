import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;
import java.lang.StringBuilder;

public class Model{
    // A map from node index to model variable name;
    public Map<String, String> nodeToVariables;
    public Node[] nodes;
    public int size;
    public boolean[][] nodeAdjacencies;
    public boolean isUniqueDigit;
    public boolean isSingleNode;
    public boolean isDirectional;
    
    public Model(boolean[][] nodeAdjacencies, Node[] initiatedNodes, boolean isUniqueDigit, boolean isSingleNode, boolean isDirectional) {
        int i = 0, j = 1;
        this.nodeToVariables = new HashMap<String, String>();
        for (Node node: initiatedNodes){
       	    for (int k : node.getCandidates()){
                nodeToVariables.put(i+"_"+k, "x"+i+"_"+(char)(k+65));
                j++;
            }
	    i++;
        }
        this.nodes = initiatedNodes;
	this.size = initiatedNodes.length;
        this.nodeAdjacencies = nodeAdjacencies;
        this.isSingleNode = isSingleNode;
        this.isUniqueDigit = isUniqueDigit;
    }
    
    public int writeModelFile(String filename) {
        int constraints = 0;
	try {
            File modelFile = new File(filename);
	    FileWriter writer;
            if (modelFile.createNewFile()) {
                System.out.println("Model file created: " + modelFile.getName());
		writer = new FileWriter(filename);
            } else {
                System.out.println("Model file already exists.");
		writer = new FileWriter(filename, false);
            }
	    System.out.println("Writing model file, usd options: " + isUniqueDigit +
			    ", " + isSingleNode + ", " + isDirectional);
            // Start writing adjacency information.
            String v1 = "";
            String v2 = "";
	    String v3 = "";
	    
	    StringBuilder adjacencyBuilder = new StringBuilder();
            for (int i = 0; i < nodes.length; i ++) {
		System.out.print("\n");
                for (int j = 0; j < nodes.length; j ++) {
		    System.out.print(nodeAdjacencies[i][j] + ", ");
                    if (nodeAdjacencies[i][j]) {
			if ((!isDirectional) && i <= j) {
			    continue;
			}
                        // Need to enumerate through possible variables.
                        for (int k: nodes[i].getCandidates()){
                            v1 = nodeToVariables.get(i+"_"+k);
			    int[] cand = nodes[i].getCandidates();
			    
                            if (IntStream.of(cand).anyMatch(a -> a == k+1)) {
                                v2 = nodeToVariables.get(j+"_"+(k+1));
				adjacencyBuilder = adjacencyBuilder.append("+1 ~" + v1 + " +1 ~" + v2 + " >= 1;\n");
				//System.out.println(adjacencyBuilder);
				constraints ++;
                            }

                            if (IntStream.of(cand).anyMatch(a -> a == k-1)) {
                                v2 = nodeToVariables.get(j+"_"+(k-1));
				adjacencyBuilder = adjacencyBuilder.append("+1 ~" + v1 + " +1 ~" + v2 + " >= 1;\n");
				//System.out.println(adjacencyBuilder);
				constraints ++;
                            }
                        }
                    }
                }
            }
            if (isUniqueDigit) {
	        constraints += (nodes[0].getCandidates().length * 2);
	    }
	    if (isSingleNode) {
	        constraints += (nodes.length * 2);
	    }
	    writer.write("* #variable= " + (nodes.length * nodes[0].getCandidates().length) + " #constraint= " + constraints + "\n");
            writer.write(adjacencyBuilder.toString());
            //Every answer can only showup in one node.
            if (isUniqueDigit) {
                for (int k: nodes[0].getCandidates()) {
                    v2 = "";
		    v3 = "";
                    for (int i = 0; i < nodes.length; i ++){
                        v1 = nodeToVariables.get(i+"_"+k);
                        v2 = v2 + "+1 ~" + v1 + " ";
			v3 = v3 + "+1 " + v1 + " ";
                    }
                    v2 = v2 + ">= " + (nodes[0].getCandidates().length-1) + ";\n";
		    v3 = v3 + ">= 1 ;\n";
                    writer.write(v2);
		    writer.write(v3);
                }
            }
            
            //Every node contains 1 and only 1 answer.
            if(isSingleNode) {
                for (int i = 0; i < nodes.length; i ++) {
                    v2 = "";
		    v3 = "";
                    for (int k: nodes[0].getCandidates()){
                        v1 = nodeToVariables.get(i+"_"+k);
                        v2 = v2 + "+1 ~" + v1 + " ";
			v3 = v3 + "+1 " + v1 + " ";
                    }
                    v2 = v2 + ">= " + (nodes[0].getCandidates().length-1) + ";\n";
		    v3 = v3 + ">= 1 ;\n";
                    writer.write(v2);
		    writer.write(v3);
                }
            }
            
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
	    return -1;
        }
        return constraints;
    }
    
    public int readModelFile() {
        return -1;
    }
}
