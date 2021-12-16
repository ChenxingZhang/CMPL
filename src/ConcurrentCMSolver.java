import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;

import java.lang.StringBuilder;


public class ConcurrentCMSolver {
	private static FileWriter writer = null;
	private static Model model = null;
	private static boolean enabledPropagation = false;
	private static ForkJoinPool lumberjack = ForkJoinPool.commonPool();

	public static void mount(FileWriter writer, Model model, boolean enabledPropagation) {
		ConcurrentCMSolver.writer = writer;
		ConcurrentCMSolver.model = model;
		ConcurrentCMSolver.enabledPropagation = enabledPropagation;
	}

	public void run() {
		if (ConcurrentCMSolver.writer == null || ConcurrentCMSolver.model == null) {
			System.out.println("Please mount the solver by Solver.mount!");
			return;
		}
		List<Integer> answers = new ArrayList<>(model.size);
		for (int i = 0; i < model.size; i ++) {
			answers.add(99);
		}
		List<Integer> initialAssignments = Arrays.stream(model.nodes[0]
				.getCandidates()).boxed().collect(Collectors.toList());
		List<ChopAction> chunks = new ArrayList<>(model.size);
		for (int i = 0; i < model.size; i ++) {
		        chunks.add(i, new ChopAction(new ArrayList(answers), 0, initialAssignments.get(i)));
		}
		System.out.println(chunks.size() + ", " + chunks.get(2));
		for (ChopAction c : ForkJoinTask.invokeAll(chunks)) {
			if (c.getException() != null) {
				c.getException().printStackTrace();
			} else {
			        System.out.println("+1 Root Thread Secured, from " + c.assignment);
			}
		}
	}

	private class ChopAction extends RecursiveAction {
		public List<Integer> vein;
		public int depth;
		public int assignment;

		private ChopAction(List<Integer> vein, int depth, int assignment) {
			this.vein = vein;
			this.depth = depth;
			this.assignment = assignment;
			
			this.vein.set(depth, assignment);
		}

		@Override
		protected void compute() {
			boolean isSolution = false;
			if (depth == ConcurrentCMSolver.model.size - 1) {
				if (Main.isAnswerAcceptable(vein, ConcurrentCMSolver
							.model)) {
					isSolution = true;
				}
			} else {
				if (Main.isAnswerAcceptable(vein, ConcurrentCMSolver
							.model)) {
					ForkJoinTask.invokeAll(chopIntoPieces());			
				}
			}
			ConcurrentCMSolver.logProofs(vein, depth, !isSolution);
		}
		
		// This private method chop the solution tree based on the possible values of the node.
		private List<ChopAction> chopIntoPieces() {
			List<Integer> chunkMarks = IntStream.range(0, model.size).boxed()
					.collect(Collectors.toList());
			if (ConcurrentCMSolver.enabledPropagation) {
				chunkMarks = chunkMarks.stream()
					.filter(c -> !vein.contains(c))
					.collect(Collectors.toList());
			}
			List<ChopAction> chunks = new ArrayList<>(chunkMarks.size());
			for (int i = 0; i < chunkMarks.size(); i ++) {
				chunks.add(i, new ChopAction(new ArrayList(vein),
							depth + 1, chunkMarks.get(i)));
			}
			return chunks;
		}
	}

	private static void logProofs(List<Integer> vein, int depth, boolean isRUP) {
		StringBuilder log = isRUP ? new StringBuilder("u ") 
			: new StringBuilder("v ");

		if (isRUP) {
			log.append(IntStream
				.range(0, vein.size())
				// Not sure if this is a cached int, but... nothing should be greater than 99, and boxed by box().
				.filter(i -> vein.get(i) != (Integer.valueOf(99)))
				.mapToObj(i -> "1 ~" + model.nodeToVariables.get(i + "_" + vein.get(i)))
				.collect(Collectors.joining(" ", "", " >= 1 ;\n")));
		} else {
			log.append(IntStream
				// No comparison because we've got a v statement.
				.range(0, vein.size())
				.mapToObj(i -> model.nodeToVariables.get(i + "_" + vein.get(i)))
				.collect(Collectors.joining(" ", "", "\n")));
		}
		try {
			ConcurrentCMSolver.writer.write(log.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
