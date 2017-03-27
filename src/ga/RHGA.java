package ga;

/**
 * Created by Jialin Liu on 26/03/17.
 * CSEE, University of Essex, UK
 * Email: jialin.liu@essex.ac.uk
 * <p>
 * Respect to Google Java Style Guide:
 * https://google.github.io/styleguide/javaguide.html
 */

import core.game.StateObservationMulti;
import evodef.EvoAlg;
import evodef.SearchSpace;
import evodef.SearchSpaceUtil;
import evodef.SolutionEvaluator;
import evogame.Mutator;
import ntuple.NTupleSystem;
import ontology.Types;
import tools.ElapsedCpuTimer;
import utilities.StatSummary;

import java.util.HashMap;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 26/02/14
 * Time: 15:17
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class RHGA implements EvoAlg {
    private int popSize = 10;
    private int simulationDepth;
    private double probaMut;
    private int nSamples;
    private int[] bestYet;
    private int[] seed;
    private boolean isShiftBuffer;
    private SearchSpace searchSpace;
    private NTupleSystem model;
    private int genome[][][][];
    private final HashMap<Integer, Types.ACTIONS>[] action_mapping;
    private final HashMap<Types.ACTIONS, Integer>[] r_action_mapping;
    static Random randomGenerator = new Random();

    private int id, oppId, no_players;

    // should not be a static, just did it quick and dirty
    static boolean noisy = false;
    static double epsilon = 1.0;
    static boolean accumulateBestYetStats = false;
    // this is only checked if not resampling parent
    static boolean resampleParent = false;

    public RHGA() {
        this(1);
    }

    public RHGA(int nSamples) {
        this(nSamples, false);
    }

    public RHGA(int nSamples, boolean isShiftBuffer) {
        this.nSamples = nSamples;
        this.isShiftBuffer = isShiftBuffer;
    }

    @Override
    public void setInitialSeed(int[] seed) {
        this.seed = seed;
    }

    @Override
    public void setModel(NTupleSystem nTupleSystem) {
        this.model = nTupleSystem;
    }

    @Override
    public NTupleSystem getModel() {
        return model;
    }

    /**
     * Init search space
     * @param evaluator
     */
    private void init(SolutionEvaluator evaluator) {
        this.searchSpace = evaluator.searchSpace();
        this.simulationDepth = searchSpace.nDims();
        this.probaMut = (double ) 1/simulationDepth;
        if (seed == null) {
            bestYet = SearchSpaceUtil.randomPoint(searchSpace);
        } else {
            bestYet = SearchSpaceUtil.copyPoint(seed);
        }
    }

    /**
     *
     * @param evaluator
     * @param maxEvals
     * @return: the solution coded as an array of int
     */
    @Override
    public int[] runTrial(SolutionEvaluator evaluator, int maxEvals) {
        init(evaluator);
        StatSummary fitBest = fitness(evaluator, bestYet, new StatSummary());
        Mutator mutator = new Mutator(searchSpace);

        while (evaluator.nEvals() < maxEvals && !evaluator.optimalFound()) {
            int[] mut = mutator.randMut(bestYet);
            // int[] mut = randMutAll(bestYet);
            // int[] mut = randAll(bestYet);
            StatSummary fitMut = fitness(evaluator, mut, new StatSummary());
            if (accumulateBestYetStats) {
                fitBest = fitness(evaluator, bestYet, fitBest);

            } else {
                if (resampleParent) {
                    fitBest = fitness(evaluator, bestYet, new StatSummary());
                }
            }
            // System.out.println(fitBest.mean() + " : " + fitMut.mean());
            if ( fitMut.mean() >= fitBest.mean()) {
                // System.out.println("Updating best");
                bestYet = mut;
                fitBest = fitMut;
                evaluator.logger().keepBest(mut, fitMut.mean());


                // now check whether it is better than optimal by epsilon
                // this is for noisy optimisation only
                if (noisy) {
                    Double opt = evaluator.optimalIfKnown();
                    if (opt != null) {
                        if (fitMut.mean() >= opt + epsilon) {
                            return bestYet;
                        }
                    }
                }
            }
        }
        // System.out.println("Ran for: " + evaluator.nEvals());
        return bestYet;
    }

    public StatSummary fitness(SolutionEvaluator evaluator, int[] sol, StatSummary ss) {
        for (int i=0; i<nSamples; i++) {
            double fitness = evaluator.evaluate(sol);
            ss.add(fitness);
        }
        if (model != null) {
            model.addSummary(sol, ss);
        }
        return ss;
    }

    public void evaluatePopulation(SolutionEvaluator evaluator) {
        for (int)
    }
    /**
     * Public constructor with state observation and time due.
     *
     * @param stateObs     state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public RHGA(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
        no_players = stateObs.getNoPlayers();
        id = playerID;
        oppID = (id + 1) % no_players;

        randomGenerator = new Random();
        N_ACTIONS = new int[no_players];

        action_mapping = new HashMap[no_players];
        r_action_mapping = new HashMap[no_players];
        for (int j = 0; j < no_players; j++) {
            action_mapping[j] = new HashMap<>();
            r_action_mapping[j] = new HashMap<>();
            int i = 0;
            for (Types.ACTIONS action : stateObs.getAvailableActions(j)) {
                action_mapping[j].put(i, action);
                r_action_mapping[j].put(action, i);
                i++;
            }

            N_ACTIONS[j] = stateObs.getAvailableActions(j).size();
        }
        initGenome(stateObs);
    }


    private void initGenome(StateObservationMulti stateObs) {
        int max = 0;
        for (int i = 0; i < stateObs.getNoPlayers(); i++) {
            if (N_ACTIONS[i] > max) max = N_ACTIONS[i];
        }
        genome = new int[stateObs.getNoPlayers()][max][POPULATION_SIZE][SIMULATION_DEPTH];
        // Randomize initial genome
        for (int i = 0; i < genome.length; i++) {
            for (int j = 0; j < genome[i].length; j++) {
                for (int k = 0; k < genome[i][j].length; k++) {
                    for (int m = 0; m < genome[i][j][k].length; m++) {
                        genome[i][j][k][m] = randomGenerator.nextInt(N_ACTIONS[i]);
                    }
                }
            }
        }
    }
}

