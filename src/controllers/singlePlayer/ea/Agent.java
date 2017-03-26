package controllers.singlePlayer.ea;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import evodef.EvoAlg;
import evodef.GameActionSpaceAdapter;
import evodef.SearchSpaceUtil;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 *
 *
 */
public class Agent extends AbstractPlayer {

    public static int MCTS_ITERATIONS = 100;
    public static double REWARD_DISCOUNT = 1.00;
    public int num_actions;
    public Types.ACTIONS[] actions;
    public static int SEQUENCE_LENGTH = 25;

    int nEvals;

    public EvoAlg evoAlg;

    public static void main(String[] args) {
        System.out.println();
    }

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer, EvoAlg evoAlg, int nEvals)
    {
        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
        num_actions = actions.length;

        System.out.println(Arrays.toString(actions));

        //Create the player.

        this.evoAlg = evoAlg;
        this.nEvals = nEvals;
        index = 0;
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */

    int index;
    int[] solution;

    // will only recalculate after this number of steps
    static int playoutLength = 1;

    public static boolean useShiftBuffer = true;

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        //Set the state observation object as the new root of the tree.

        // we'll set up a game adapter and run the algorithm independently each
        // time at least to being with

        int action;
            GameActionSpaceAdapter game = new GameActionSpaceAdapter(stateObs, SEQUENCE_LENGTH);

        if (solution != null) {
            solution = SearchSpaceUtil.shiftLeftAndRandomAppend(solution, game);
            evoAlg.setInitialSeed(solution);
        }

        solution = evoAlg.runTrial(game, nEvals);

        // System.out.println(Arrays.toString(solution) + "\t " + game.evaluate(solution) + "\t " + useShiftBuffer);

        action = solution[0];
        // already return the first element, so now set it to 1 ...

        if (!useShiftBuffer) solution = null;

        index = 1;

        //... and return it.
        return actions[action];
    }

    public Types.ACTIONS actOld(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        //Set the state observation object as the new root of the tree.

        // we'll set up a game adapter and run the algorithm independently each
        // time at least to being with

        int action;
        if (index > 0) {
            // means we've already set it up, now let's play it
            action = solution[index];
            index++;
            index %= playoutLength;
        } else {
            GameActionSpaceAdapter game = new GameActionSpaceAdapter(stateObs, SEQUENCE_LENGTH);

            solution = evoAlg.runTrial(game, nEvals);

            System.out.println(Arrays.toString(solution) + "\t " + game.evaluate(solution));

            action = solution[0];
            // already return the first element, so now set it to 1 ...
            index = 1;
        }

        //... and return it.
        return actions[action];
    }

}
