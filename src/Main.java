import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.Scanner;
import java.util.Date;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Main
{
    /*
     * Args:
     * arg0 - filename.
     * arg1 = options without spaces, available options include:
     * c - concurrent
     * p - propagation
     * r - prune logs
     **/
    public static void main(String[] args) {
    
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: 'java Main [filename] [options_no_space]'");
        }
        String fname = args[0];
        String options = args.length > 1 ? args[1] : "";
        boolean isParallel = options.contains("c");
        boolean isPropagation = options.contains("p");
	boolean isPruning = options.contains("l");
        System.out.println("Solver started with: Concurrency - " + isParallel
            + ", Propagation - " + isPropagation + ", Pruning - " + isPruning);
	if (isParallel && isPruning) {
	    System.out.println("Concurrent solver does not support log pruning. Leaving the field.");
	    return;
	}
        //Start timing after reading the args.
        long startingTime = new Date().getTime();

        String graphProperties = "usd -1";
        Scanner scanner;
        boolean[][] nodeAdjacencies = new boolean[1][1];
        Node[] nodes = new Node[8];
        boolean isUniqueLetter = true;
        boolean isSingleNode = true;
        boolean isDirectional = false;

        try {
            File graphFile = new File(fname);
            scanner = new Scanner(graphFile);
            graphProperties = scanner.nextLine();
            int size = Integer.parseInt(graphProperties.split(" ")[1]);
            if (size < 0) {
                System.out.println("Failed parsing graph properties.");
            }
            nodes = new Node[size];
            for (int i = 0; i < size; i ++) {
                nodes[i] = new Node(size);
            }
            isUniqueLetter = graphProperties.contains("u");
            isSingleNode = graphProperties.contains("s");
            isDirectional = graphProperties.contains("d");
            nodeAdjacencies = readAdjacencyFromFile(fname, scanner, size);
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the file.");
            e.printStackTrace();
        }

        int size = Integer.parseInt(graphProperties.split(" ")[1]);
        if (size < 0) {
            System.out.println("Failed parsing graph properties.");
        }
    
        System.out.println("Loaded graph with size " + nodeAdjacencies.length + 
            ". Options: " + isUniqueLetter + ", " + isSingleNode + 
            ", " + isDirectional + ". ");
        if (!isSingleNode) {
            System.out.println("[WARN] Single node is only for PL System tests, the"
                    + " solver does not support the property and will assume it.");
        }
        
        Model model = new Model(nodeAdjacencies, nodes, isUniqueLetter, isSingleNode, isDirectional);
        int constraints = model.writeModelFile(fname + ".opb");
        if (constraints < 1) {
            System.out.println("Check the model generation. The model cannot be recognised.");
            return;
        }
        List<Integer>[] res = new List[20]; // For 20 correct answers.
        int resCount = 0; // Counting how many answers we've got.
        int[] arrayAnswers = new int[size];
        for (int i = 0; i < size; i ++) {
            arrayAnswers[i] = 99;
        }
        List<Integer> answers = Arrays.stream(arrayAnswers)
                                            .boxed()
                                            .collect(Collectors.toList());
        
        int newAns = -1;
        int depth = 0; // Pointing to the current working node layer of the tree.
	int lastWipedLevel = 0;
        try {
            File proofFile = new File(fname + ".proof");
            FileWriter writer;
            if (proofFile.createNewFile()) {
                System.out.println("Proof file created: " + proofFile.getName());
                writer = new FileWriter(fname + ".proof");
            } else {
                System.out.print("Proof file already exists.\n");
                writer = new FileWriter(fname + ".proof", false);
            }
            writer.write("pseudo-Boolean proof version 1.0\n");
            writer.write("f " + constraints + "\n");
        
            if (isParallel) {
                ConcurrentCMSolver.mount(writer, model, isPropagation);
                (new ConcurrentCMSolver()).run();
            } else {
                while (depth < size) {
                    if (isPropagation) {
                        newAns = nodes[depth].popCandidate(answers, isParallel);
                    } else {
                        newAns = nodes[depth].popCandidate(isParallel);
                    }
                    if (newAns < 0) { // -1 if ran out of available answer for this node, we need to go up one level.
                        if (depth == 0) {
                            // the tree is traversed and dead. Hopefully we've recorded all the answers in res.
                            break;
                        }
                        // reset the node since its parent is going to change (even if it's dying).
                        nodes[depth].resetCandidates();
                        // We also write down this as reverse unit propagation step in the logger.
                        int l = 0;
			if (isPruning) {
			    writer.write("# " + depth + "\n");
			    lastWipedLevel = depth;
			}
                        writer.write("u ");
                        for (Integer ans : answers) {
                            if (ans >= 99) {
                                break;
                            }
                            writer.write("1 ~" + model.nodeToVariables.get(l + "_" + ans)+ " ");
                            l ++;
                        }
                        writer.write(">= 1 ;\n");
			
                        answers.set(depth, 99);
			// Its parent is changing.
                        answers.set(depth -1, 99);
			//Wipe out the logs at current level.
			if (isPruning && lastWipedLevel != depth) {
			    writer.write("w " + (depth + 1) + "\n");
			    lastWipedLevel = depth + 1;
			}
                        depth --;
                        continue;
                    }
                    answers.set(depth, newAns);
                    if (isAnswerAcceptable(answers, model)) {
                        if (depth == (size-1)) {
                            if (resCount < res.length) {
                                res[resCount ++] = answers;
                            }
                            writer.write("v ");
                            int d = 0;
                            for (Integer l : answers) {
                                if (l >= 99) {
                                    break;
                                }
                                writer.write(model.nodeToVariables.get(d + "_" + l) + " ");
                                d ++;
                            }
                            writer.write("\n");
                            continue;
                        }
                        depth ++;
                    } else {
                        int l = 0;
			if (isPruning) {
			    writer.write("# " + (depth + 1) + "\n");
			    lastWipedLevel = depth + 1;
			}
                        writer.write("u ");
                        for (Integer ans : answers) {
                            if (ans >= 99) {
                                break;
                            }
                            writer.write("1 ~" + model.nodeToVariables.get(l + "_" + ans)+ " ");
                            l ++;
                        }
                        writer.write(">= 1 ;\n");
                        answers.set(depth, 99);
                        continue;
                    }
                }
            }
            writer.write("u >= 1 ;\n");
            writer.write("c -1\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        long endingTime = new Date().getTime();
        System.out.println("The total running time in seconds: " + 
            ((double)(endingTime - startingTime))/1000.0);
    }
    
    /*
     * ans - list of applied assignments to check
     * nodeAdjacencies - adjacency matrix
     * isDirectional - if the graph is directional
     **/
    public static boolean isAnswerAcceptable(List<Integer> ans, Model model) {
    for(int i = 0; i < model.nodeAdjacencies.length; i ++) {
            for(int j = 0; j < model.nodeAdjacencies.length; j ++) {
        // Used the same letter.
        if(i != j && ans.get(i) < 99 && ans.get(i) - ans.get(j) == 0) {
            return false;
        }
        // Adjacent letters used as neighbours.
        boolean adjacent = model.isDirectional ? (i < j ? (model.nodeAdjacencies[j][i]) : model.nodeAdjacencies[i][j]) : model.nodeAdjacencies[i][j];
                if(adjacent) {
                    if(ans.get(i) - ans.get(j) == 1 || ans.get(i) - ans.get(j) == -1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean[][] readAdjacencyFromFile(String fname, Scanner s, int size) {
        boolean[][] a = new boolean[size][size];
        int i = 0;
        while (s.hasNextLine()) {
            String[] line = s.nextLine().split(",");
            for (int j = 0; j < size; j ++) {
                a[i][j] = line[j].equals("true");
                System.out.print(a[i][j] + ", ");
            }
            i ++;
            System.out.print("\n");
        }
        s.close();
        return a;
    }
}
