import java.util.Random;
import java.util.Arrays;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main{
    public static void main (String args[]) {
        System.out.println("ModelGen is starting...");
	String fname = args[0];
	String options = "";
	int size = args[1].equals("cm") ? 8 : Integer.parseInt(args[1]);
	if (args.length > 2) {
	    options = args[2];
	} else if (args.length < 1 || args.length > 4){
	    System.out.print("Usage: java Main [filename] [size or 'cm'] [options]");
	    return;
	}
	boolean isCrystalMaze = (args[1].equals("cm") || options.contains("cm"));
	boolean isDirectional = options.contains("d");
        boolean[][] adjacency;
	System.out.println("Read the options. Writting to " + fname 
			+ ", with options " + options);
        try {
            File mfile = new File(fname);
	    FileWriter writer;
	    if (mfile.createNewFile()) {
	        writer = new FileWriter(fname);
	    } else {
	    	System.out.println("Name already exists, please use another name!");
		return;
	    }
	    writer.write(options + " " + size + "\n");
	    if (isCrystalMaze) {
	        adjacency = crystalMazeMatrix();
	    } else {
	        adjacency = randomAdjacencyMatrixOfSize(size, isDirectional);
	    }

	    writeModel(adjacency, writer);
	    writer.close();
	} catch (IOException e) {
	    System.out.print("Encountered IOException.");
	    e.printStackTrace();
	    return;
	}
	return;
    }

    private static boolean[][] randomAdjacencyMatrixOfSize(int size, boolean isDirectional) {
    	boolean[][] ans = new boolean[size][size];
	Random r = new Random();
	for (int i = 0; i < size; i ++) {
	    for (int j = 0; j < size; j ++) {
		if (i == j) {
		    ans[i][i] = false;
		    continue;
		}
	        ans[i][j] = (r.nextInt(2)==1);
	    }
	}

	// Unnecessary, but just in case if some one what to check the content of the adjacency matrix and really want to see it setup correctly...
	if (!isDirectional) {
	    for (int i = 0; i < size; i ++) {
	        for (int j = 0; j < size; j ++) {
		    if (i < j) {
		        ans[i][j] = ans[j][i];
		    }
		}
	    }
	}
        return ans;
    }

    private static boolean[][] crystalMazeMatrix() {
        return new boolean[][] {{false, true, true, true, false, false, false, false},
	        {true, false, true, false, true, true, false, false},
		{true, true, false, true, true, true, true, false},
		{true, false, true, false, false, true, true, false},
		{false, true, true, false, false, true, false, true},
		{false, true, true, true, true, false, true, true},
		{false, false, true, true, false, true, false, true},
		{false, false, false, false, true, true, true, false}};
    }

    private static void writeModel(boolean[][] a, FileWriter writer) throws IOException{
	String[] line = new String[a.length];
	for (int i = 0; i < a.length; i ++) {
	    for (int j = 0; j < a.length; j ++) {
	        line[j] = a[i][j] ? "true" : "false";
	    }
	    writer.write(String.join(",", line) + "\n");
	}
	return;
    }
}
