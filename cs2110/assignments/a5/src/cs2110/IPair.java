package cs2110;

/**
 * An ordered pair of integers (`i`, `j`).
 */
public record IPair(int i, int j) {
    // A "record" class will automatically generate a constructor `IPair(i, j)` and
    //  accessors `i()` and `j()`, as well as overrides for `toString()`, `equals()`, and
    //  `hashCode()`.  It is otherwise just a nested, static (not inner) class.
}
