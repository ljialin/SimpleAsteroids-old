package numbergame;

import battle.SimpleBattleState;
import core.game.ForwardModel;
import core.game.StateObservationMulti;
import gvglink.SpaceBattleLinkStateTwoPlayer;
import ontology.Types;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by simonmarklucas on 06/03/2017.
 */


public class DiffGame extends StateObservationMulti {

    // game of length 50 should be enough
    static int maxTick = 50;
    // game tick counter
    public static int nTicks = 0;
    // values in the game
    public static int nValues = 11;
    public static int minscore = 0;

    int tick, score;
    int i1, i2; // positions of players 1 and 2
    static Random random = new Random();

    static Types.WINNER[] noWinners = new Types.WINNER[]{Types.WINNER.NO_WINNER, Types.WINNER.NO_WINNER};

    static int actionLimit = 3; // nb of available actions in the game

    static ArrayList<Types.ACTIONS> actions = new ArrayList<>();
    static {
        for (Types.ACTIONS a : Types.ACTIONS.values()) {
            if (actions.size() < actionLimit) {
//                System.out.println("Types.ACTIONS a=" + a.toString());
                actions.add(a);
            }
        }
        System.out.println("nActions = " + actions.size());
    }

    public static void main(String[] args) {

        minscore = 3;
        // nValues = 31;
        for (int i=0; i<nValues; i++) {
            for (int j=0; j<nValues; j++) {
                System.out.format("%d\t %d\t %d\n", i, j, getScore(i, j));
            }
        }
        // now do a speed test
    }

    static int getScore(int i1, int i2) {
        int rawScore = (i1 - i2 + nValues + nValues/2) % nValues - nValues/2;
        if (Math.abs(rawScore) < minscore) return 0;
        else return rawScore;

    }

    public DiffGame(ForwardModel a_model) {
        super(a_model);
    }

    public DiffGame() {
        // this(0, 0, 0, nValues/2);
        this(0, 0, 0, 0);
    }

    public DiffGame(int tick, int score, int i1, int i2) {
        super(null);
        this.tick = tick;
        this.score = score;
        this.i1 = i1;
        this.i2 = i2;
    }

    public String toString() {
        return String.format("%d\t %d\t %d\t %d ; scoreDiff = %d\n", tick, i1, i2, score, getScore(i1, i2));
    }

    public StateObservationMulti copy() {
        return new DiffGame(tick, score, i1, i2);
    }

    // static int[] scores = new int[Types.ACTIONS]


    public void advance(Types.ACTIONS action) {
        throw new RuntimeException("Not meant to call single player version");
    }

    /**
     * Advance the game given the actions of both players
     *
     * @param actions
     */
    public void advance(Types.ACTIONS[] actions) {
        int[] a = new int[]{actions[0].ordinal(), actions[1].ordinal()};
        // translate the move
        int a1 = getMove(actions[0].ordinal());
        int a2 = getMove(actions[1].ordinal());
        // update the position
        i1 = (i1 + nValues + a1) % nValues;
        i2 = (i2 + nValues + a2) % nValues;
        // calculate the score
        score += getScore(i1, i2);
        // game ticks consumed
        tick++;

        // keep track of total game ticks used
        nTicks++;
    }

    /**
     * Translate the action index by half of the range
     *
     * @param action
     * @return
     */
    static int getMove(int action) {
        return action - actionLimit / 2;
    }

    /**
     * Sets a new seed for the forward model's random generator (creates a new object)
     *
     * @param seed the new seed.
     */
    public void setNewSeed(int seed) {
        System.out.println("Not setting new seed...");
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar.
     *
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions() {
        return actions;
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar. If the parameter 'includeNIL' is true, the array contains the (always available)
     * NIL action. If it is false, this is equivalent to calling getAvailableActions().
     *
     * @param includeNIL true to include Types.ACTIONS.ACTION_NIL in the array of actions.
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions(boolean includeNIL) {
        return actions;
    }

    public ArrayList<Types.ACTIONS> getAvailableActions(int playerID) {
        return actions;
    }



    public int getNoPlayers() {
        return 2;
    }

    public double getGameScore() {
        return getGameScore(0);
    }

    public double getGameScore(int playerId) {
        if (playerId == 0) {
            return score;
        } else {
            return -score;
        }
    }

    public int getGameTick() {
        return tick;
    }

    /**
     * Indicates if there is a game winner in the current observation.
     * Possible values are Types.WINNER.PLAYER_WINS, Types.WINNER.PLAYER_LOSES and
     * Types.WINNER.NO_WINNER.
     *
     * @return the winner of the game.
     */
    public Types.WINNER getGameWinner() {
        return Types.WINNER.NO_WINNER;
    }

    public Types.WINNER[] getMultiGameWinner() {
        return noWinners;
    }

    /**
     * Indicates if the game is over or if it hasn't finished yet.
     *
     * @return true if the game is over.
     */
    public boolean isGameOver() {
        return tick >= maxTick;
    }


}
