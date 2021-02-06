package enigma;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Deep Dayaramani
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        _permutation = perm;
    }
    @Override
    boolean rotates() {
        return true;
    }

    /** Return true iff I reflect. */
    @Override
    boolean reflecting() {
        return false;
    }

    @Override
    boolean atNotch() {
        for (int i = 0; i < _notches.length(); i++) {
            if (_notches.charAt(i)
                    == _permutation.alphabet().toChar(this.setting())) {
                return true;
            }
        }
        return false;
    }


    @Override
    void advance() {
        this.set(_permutation.wrap(this.setting() + 1));
    }

    /** Stores the string Notches for the Moving Rotor.*/
    private String _notches;
    /** Permutation for the Moving Rotor. */
    private Permutation _permutation;
}
