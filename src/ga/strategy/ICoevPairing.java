package ga.strategy;

import evodef.SolutionEvaluator;
import ga.search.Individual;

/**
 * Created by dperez on 08/07/15.
 */
public abstract class ICoevPairing
{
    public int groupSize;

    public abstract double evaluate(SolutionEvaluator evaluator, Individual individual, Individual[] otherPop);
    public int getGroupSize() {
        return groupSize;
    }
}