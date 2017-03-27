package ga.strategy;

import ga.search.Individual;

/**
 * Created by dperez on 08/07/15.
 */
public interface ICrossover
{
    Individual uniformCross(Individual parent1, Individual parent2);
}