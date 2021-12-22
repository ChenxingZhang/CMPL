# CMPL
Solution for Crystal-Maze-like problems with two simple solvers integrated with proof logging system. 

## Usages
The solvers consume .cm files, and produces .proof logs. It will translate .cm files to .opb file as well.
To verify the logs you'll need [VeriPB](https://github.com/StephanGocht/VeriPB#set-level)
VeriPB installation can be found on its main page. To run it:
```
veripb cm.cm.opb cm.cm.proof
```

### src/Main
This is the entry point for solvers.
```java Main [modelFile_name] [options]```

#### Options:
c - concurrent

p - propagation

l - log prune


#### Example uses:
For a plain solver with proofs:
```java Main cm.cm```
For a concurrent solver with propagation:
```java Main cm.cm cp```
Note that concurrent and log prune (l) is not compatible;
And you don't need a dash for the options.

### modelGen/Main
This is the model generator.

#### Options:
u - unique letters.

s - single node capacity.

d - directional edges.

#### Example usages:
For a 9x9 cm puzzle:
```java Main [any_name.cm] 9 usd```
For a 12x12 puzzle with no unique letter limit:
```java Main 12_12_not_unique.cm 12 sd```

## Classes

### src/Main.java
Main entry and the single thread solver.

### src/Node.java
Node class, for specific node objects. It manages propagation as well.

### src/Model.java
This is the model object which holds the adjacency matrices and the translation between variables and node-letter pairs.

### src/ConcurrentCMSolver.java
The concurrent solver.

### modelGen/Main.java
The model generator.

## Links
VeriPB - https://github.com/StephanGocht/VeriPB#set-level

### Dissertation
