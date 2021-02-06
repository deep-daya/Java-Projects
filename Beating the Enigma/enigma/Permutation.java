package enigma;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Deep Dayaramani
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String toReplace = cycles.replace(")", " ");
        toReplace = toReplace.replace("(", "");
        _cycles = toReplace.split(" +");
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        String [] newCycles = new String[_cycles.length + 1];
        System.arraycopy(_cycles, 0, newCycles, 0, _cycles.length);
        newCycles[_cycles.length + 1] = cycle;
        _cycles = newCycles;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Return the P mod SIZE for any given p and size
     * for any p and size and serves as an alternative
     * for the wrap function. */
    int mod(int p, int size) {
        int r = p % size;
        if (r < 0) {
            r += size;
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char toPermute = _alphabet.toChar(wrap(p));
        int permutedInt;
        for (int i = 0; i < _cycles.length; i++) {
            if (_cycles[i].indexOf(toPermute) >= 0) {
                for (int j = 0; j < _cycles[i].length(); j++) {
                    if (_cycles[i].charAt(j) == toPermute) {
                        permutedInt = _alphabet.toInt(_cycles[i].charAt(mod(
                                j + 1, _cycles[i].length())));
                        return permutedInt;
                    }
                }
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char toInvert = _alphabet.toChar(wrap(c));
        int invertedInt;
        for (int i = 0; i < _cycles.length; i++) {
            if (_cycles[i].indexOf(toInvert) >= 0) {
                for (int j = 0; j < _cycles[i].length(); j++) {
                    if (_cycles[i].charAt(j) == toInvert) {
                        invertedInt = _alphabet.toInt(_cycles[i].charAt(
                                mod(j - 1, _cycles[i].length())));
                        return invertedInt;
                    }
                }
            }
        }
        return c;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int permuteIndex = _alphabet.toInt(p);
        if (permuteIndex == -1) {
            throw EnigmaException.error("OOO U did an oopsie");
        }
        return _alphabet.toChar(permute(permuteIndex));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int invertIndex = _alphabet.toInt(c);
        if (invertIndex == -1) {
            throw EnigmaException.error("OOO U did an oopsie");
        }
        return _alphabet.toChar(invert(invertIndex));

    }
    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int mapItself = 0;
        for (int i = 0; i < _cycles.length; i++) {
            mapItself += _cycles[i].length();
        }
        if (mapItself < _alphabet.size()) {
            return true;
        }
        return false;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    /** Cycles of this permutation. */
    private String[] _cycles;

}
