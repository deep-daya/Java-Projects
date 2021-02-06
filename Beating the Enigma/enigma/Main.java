package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Deep Dayaramani
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        if (!(_input.hasNextLine())) {
            throw new EnigmaException("Why are u giving me nothing?");
        }
        String line  = _input.nextLine();
        if (!line.contains("*")) {
            throw new EnigmaException("Wrong Settings Format");
        }
        setUp(enigma, line);
        ArrayList<String> out = new ArrayList<>();
        int count = 0;
        while (_input.hasNextLine()) {
            if (_input.hasNext("\\*")) {
                line = _input.nextLine();
                if (line.equals("")) {
                    printMessageLine("");
                    setUp(enigma, _input.nextLine());
                    break;
                }
                setUp(enigma, line);
                break;
            }
            out.add((_input.nextLine().replace(" ", "")));
            for (int i = 0; i < out.get(count).length(); i++) {
                if (!(_alphabet.contains(out.get(count).charAt(i)))) {
                    throw new EnigmaException("Letter not part of this realm");
                }
            }
            printMessageLine(enigma.convert(out.get(count)));
            count++;
        }
        while (_input.hasNextLine()) {
            if (_input.hasNext("\\*")) {
                line = _input.nextLine();
                if (line.equals("")) {
                    printMessageLine("");
                    setUp(enigma, _input.nextLine());
                    break;
                }
                setUp(enigma, line);
                break;
            }
            printMessageLine(enigma.convert(
                    _input.nextLine().replace("\\s+", "")));
        }
        while (_input.hasNextLine()) {
            printMessageLine(enigma.convert(
                    _input.nextLine().replace("\\s+", "")));
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            int numRotors = _config.nextInt();
            int pawls = _config.nextInt();
            Collection<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String mFr = _config.next();
            char mFr2 = mFr.charAt(0);
            String notches = mFr.substring(1);
            String cycles = "";
            if (_config.hasNext("(\\(\\w+\\)){2,}")) {
                cycles += _config.next();
            }
            while (_config.hasNext("\\(\\w+\\.*\\w*\\)")) {
                cycles += _config.next();
            }
            if (mFr2 == 'M') {
                return new MovingRotor(name, new Permutation(cycles, _alphabet),
                        notches);
            } else if (mFr2 == 'N') {
                return new FixedRotor(name, new Permutation(cycles, _alphabet));
            } else {
                return new Reflector(name, new Permutation(cycles, _alphabet));
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner settingsS = new Scanner(settings);
        String[] rotors = new String[M.numRotors()];
        String setting = "";
        String plugboard = "";
        if (settingsS.next().equals("*")) {
            for (int i = 0; i < M.numRotors(); i++) {
                rotors[i] = settingsS.next();
            }
            if (rotors.length != M.numRotors()) {
                throw new EnigmaException("Wrong Number of Rotors");
            }
            M.insertRotors(rotors);
            setting += settingsS.next();
            if (setting.length() != M.numRotors() - 1) {
                throw new EnigmaException("Wrong Settings Length");
            }
            for (int i = 0; i < setting.length(); i++) {
                if (!(_alphabet.contains(setting.charAt(i)))) {
                    throw new EnigmaException("You done given "
                            + "me the wrong letter boi");
                }
            }
            M.setRotors(setting);
            boolean hasNext = settingsS.hasNext("\\(\\w+\\)");
            String ring = "";
            if (!hasNext && settingsS.hasNext()) {
                String line = settingsS.next();
                if (!line.isEmpty()) {
                    ring = line;
                }
            }
            if (!ring.isEmpty()) {
                M.setRing(ring);
            }
            boolean hasNext2 = settingsS.hasNext("\\(\\w+\\)");
            while (hasNext2) {
                plugboard += settingsS.next();
                hasNext2 = settingsS.hasNext("\\(\\w+\\)");
            }
            M.setPlugboard(new Permutation(plugboard, _alphabet));
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int count = 0;
        for (int i = 0; i < msg.length(); i++) {
            if (i % 5 == 0 && i != 0) {
                msg = msg.substring(0, i + count) + " "
                        + msg.substring(i + count, msg.length());
                String cmsg = msg.substring(i + count, msg.length());
                if (cmsg.length() <= 5) {
                    break;
                }
                count++;
            }
        }
        _output.println(msg.trim());
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
