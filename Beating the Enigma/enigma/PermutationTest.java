package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Deep Dayaramani
 */
/**
 * For this lab, you must use this to get a new Permutation,
 * the equivalent to:
 * new Permutation(cycles, alphabet)
 * @return a Permutation with cycles as its cycles and alphabet as
 * its alphabet
 * @see Permutation for description of the Permutation conctructor
 */
public class PermutationTest {
    Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        return new enigma.Permutation(cycles, alphabet);
    }

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet(chars)
     * @return an Alphabet with chars as its characters
     * @see Alphabet for description of the Alphabet constructor
     */
    Alphabet getNewAlphabet(String chars) {
        return new enigma.Alphabet(chars);
    }

    /**
     * For this lab, you must use this to get a new Alphabet,
     * the equivalent to:
     * new Alphabet()
     * @return a default Alphabet with characters ABCD...Z
     * @see Alphabet for description of the Alphabet constructor
     */
    Alphabet getNewAlphabet() {
        return new enigma.Alphabet();
    };

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation _perm;
    private String _alpha = UPPER_STRING;

    /** Check that PERM has an ALPHABET whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha,
                           Permutation perm, Alphabet alpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.toInt(c), ei = alpha.toInt(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        Alphabet alpha = getNewAlphabet();
        Permutation perm = getNewPermutation("", alpha);
        checkPerm("identity", UPPER_STRING, UPPER_STRING, perm, alpha);
    }
    @Test
    public void testInvertChar() {
        Alphabet alphaB = getNewAlphabet("ABCD");
        Permutation p = getNewPermutation("(BA)(CD)", getNewAlphabet("ABCD"));
        assertEquals('B', p.invert('A'));
        assertEquals('A', p.invert('B'));
        checkPerm("identity", "ABCD", "BADC", p, alphaB);
    }
    @Test(expected = EnigmaException.class)
    public void testNotInAlphabet() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        p.invert('F');
    }
    @Test
    public void testHilfinger() {
        Alphabet alphaB = getNewAlphabet("HILFNGR");
        Permutation p = getNewPermutation("(HIG) (NF) (L)", alphaB);
        checkPerm("identity", "HILFNGR", "IGLNFHR", p, alphaB);
    }
    @Test
    public void testInvert() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals(1, p.invert(0));
        assertEquals(3, p.invert(1));
        assertEquals(0, p.invert(2));
        assertEquals(2, p.invert(3));
        assertEquals(1, p.invert(4));
        assertEquals(3, p.invert(5));
        assertEquals(0, p.invert(6));
        assertEquals(2, p.invert(7));
        assertEquals(2, p.invert(-1));
        assertEquals(0, p.invert(-2));
        assertEquals(3, p.invert(-3));
        assertEquals(1, p.invert(-4));
    }

}
