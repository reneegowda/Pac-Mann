package util;

import java.util.Random;

/**
 * Provides random number generator seeds to support reproducible randomness that is robust to
 * different compositions of submodels with unique string identifiers.
 *
 * @param seed The seed of this source of randomness.  Values derived from this source are
 *             deterministic for a given seed and ID.
 */
public record Randomness(long seed) {

    /**
     * Return a new source of randomness that is next (after this one) in a deterministic sequence
     * of randomness sources.  This should only be called once on a source, as returned values are
     * identical.  This is useful for programs that want to run a sequence of independent
     * simulations that is reproducible from a single initial seed.
     */
    public Randomness next() {
        return new Randomness(seed + 1);
    }

    /**
     * Return a new source of randomness to be used by a submodel with ID `id`.  This facilitates
     * hierarchical submodels.
     */
    public Randomness randomnessFor(String id) {
        return new Randomness(seedFor(id));
    }

    /**
     * Return an RNG seed to be used by a submodel with ID `id`.
     */
    public long seedFor(String id) {
        return seed ^ id.hashCode();
    }

    /**
     * Return a random number generator to be used by a submodel with ID `id`.
     */
    public Random generatorFor(String id) {
        return new Random(seed ^ id.hashCode());
    }
}
