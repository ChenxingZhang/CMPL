import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private int[] candidates;
    private List<Integer> deadCandidates;
    private int resident;

    public Node(int states) {
        this.candidates = new int[states];
	for (int i = 0; i < states; i ++) {
	    this.candidates[i] = i;
	}
        this.resident = -1;
        this.deadCandidates = new ArrayList<Integer>(states);
    }
    
    public int getCurrentResident() {
        return this.resident;
    }
    
    public int popCandidate(boolean isParallel) {
        if (this.deadCandidates.size() == this.candidates.length) {
            return -1;
        }
	int[] validCandidates = new int[this.candidates.length];
	validCandidates = Arrays.stream(this.candidates).filter(c ->
			!this.deadCandidates.contains(Integer.valueOf(c))).toArray();
//	Multi-threading this petty task is too costly, that can actualy slow down the 
//	process.
//	    validCandidates = Arrays.stream(this.candidates).parallel()
//		    .filter(c -> !this.deadCandidates.contains(Integer.valueOf(c)))
//		    .toArray();
	if (validCandidates.length == 0) {
	    this.resident = -1;
	    return -1;
	}
        this.resident = validCandidates[0];
        this.deadCandidates.add(this.resident);
        return this.resident;
    }    
    
    public int popCandidate(List<Integer> removeList, boolean isParallel) {
        if (this.deadCandidates.size() == this.candidates.length) {
            this.resident = -1;
            return -1;
        }
	int[] validCandidates = new int[this.candidates.length];
        validCandidates = Arrays.stream(this.candidates)
		.filter(c -> !removeList.contains(Integer.valueOf(c))).filter(c -> 
                !this.deadCandidates.contains(Integer.valueOf(c))).toArray();
//	Multi-threading this task will actually slow down the process because of 
//	overheads.
//	    validCandidates = Arrays.stream(this.candidates).parallel()
//		    .filter(c -> !removeList.contains(Integer.valueOf(c)))
//		    .filter(c -> !this.deadCandidates.contains(Integer.valueOf(c)))
//		    .toArray();	
	if (validCandidates.length == 0) {
            this.resident = -1;
            return -1;
        }
        this.resident = validCandidates[0];
        this.deadCandidates.add(this.resident);
        return this.resident;
    }
    
    public void resetCandidates() {
        this.candidates = new int[this.candidates.length];
	for (int i = 0; i < this.candidates.length; i ++) {
	    this.candidates[i] = i;
	}
        this.deadCandidates = new ArrayList<Integer>(this.candidates.length);
        this.resident = -1;
    }
    
    public int[] getCandidates() {
        return this.candidates;
    }
}
