package enigma;

import java.util.Collection;

/** Class that represents a complete enigma machine.
 *  @author Deep Dayaramani
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors.toArray();

    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotors = new Rotor[_numRotors];
        for (int i = 0; i < rotors.length; i++) {
            for (int j = 0; j < _allRotors.length; j++) {
                if (rotors[i].equals(((Rotor) _allRotors[j]).name())) {
                    _rotors[i] = (Rotor) _allRotors[j];
                }
            }
            if (_rotors[i] == null) {
                throw new EnigmaException("Bad Rotor Name");
            }
        }
        if (!_rotors[0].reflecting()) {
            throw new EnigmaException("Reflector at Wrong Position");
        }
        for (int i = 0; i < _rotors.length; i++) {
            for (int j = _rotors.length - 1; j > i; j--) {
                if (_rotors[i].name().equals(_rotors[j].name())) {
                    throw new EnigmaException("Rotor Name Repeated");
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 1; i < _rotors.length; i++) {
            _rotors[i].set(setting.charAt(i - 1));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Set the ring to RING. */
    void setRing(String ring) {
        for (int i = 1; i < _numRotors; i++) {
            _rotors[i].setRingSetting(ring.charAt(i - 1));
        }
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean[] ifRotate = new boolean[_numRotors];
        if (_plugboard == null) {
            _plugboard = new Permutation("", _alphabet);
        }
        ifRotate[_numRotors - 1] = true;
        for (int i = _numRotors - 1; i > 0; i--) {
            if (_rotors[i].atNotch()) {
                if ((_rotors[i - 1].rotates())) {
                    ifRotate[i - 1] = true;
                    if (_rotors[i].rotates()) {
                        ifRotate[i] = true;
                    }
                }
            }
        }
        int j = 0;
        while (j < _numRotors) {
            if (ifRotate[j]) {
                _rotors[j].advance();
            }
            j++;
        }
        int convRot = _plugboard.permute(c);
        for (int i = _numRotors - 1; i >= 0; i--) {
            convRot = _rotors[i].convertForward(convRot);
        }
        for (int i = 1; i < _numRotors; i++) {
            convRot = _rotors[i].convertBackward(convRot);
        }
        convRot = _plugboard.permute(convRot);
        return convRot;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replace(" ", "");
        String msgOut = "";
        for (int i = 0; i < msg.length(); i++) {
            int convNo = convert(_alphabet.toInt(msg.charAt(i)));
            msgOut += _alphabet.toChar(convNo);
        }
        return msgOut;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    /** Number of Rotors for my rotors. */
    private int _numRotors;
    /** Number of pawls for my Machine. */
    private int _pawls;
    /** All Rotors available to set up the Machine. */
    private Object[] _allRotors;
    /** Set of Rotors for this Machine. */
    private Rotor[] _rotors;
    /** Plugboard for this Machine. */
    private Permutation _plugboard;

}
