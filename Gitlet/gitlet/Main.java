package gitlet;
import java.io.File;
/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Deep Dayaramani
 */
public class Main {


    /** Current Working Directory File. */
    static final File CWD = new File(".");
    /** Gitlet Directory Path. */
    static final String GITLET_DIRECTORY = ".gitlet/";
    /** Command Center Path. */
    static final String COMMANDCENTER_PATH =  ".gitlet/cmdcenter/";
    /**Commit Path. */
    static final String COMMIT_PATH =  ".gitlet/commits/";
    /** Blob Path. */
    static final String BLOB_PATH = ".gitlet/blobs/";

    /**Init function to initialize directories. */
    private static void init() {
        File gitlet = Utils.join(CWD, GITLET_DIRECTORY);
        File commit = Utils.join(CWD, COMMIT_PATH);
        File blob = Utils.join(CWD, BLOB_PATH);
        if (gitlet.exists()) {
            System.out.println("A Gitlet version-control"
                    + " system already exists in the current directory.");
            System.exit(0);
        }
        boolean create = gitlet.mkdir();
        commit.mkdir();
        blob.mkdir();
        CommandCenter congress = new CommandCenter();
        congress.saveToFile();
    }
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String commandInput = args[0];
        if (commandInput.equals("init")) {
            init();
        } else {
            File gitlet = Utils.join(CWD, GITLET_DIRECTORY);
            if (!gitlet.exists()) {
                System.out.println("Not in an initialized "
                        + "Gitlet directory.");
                System.exit(0);
            }
            CommandCenter congress = getCommandCenter();
            switch (args[0]) {
            case "add":
                incorrectOperandsFunc(2, args);
                congress.add(args[1]);
                break;
            case "commit":
                commitFunc(congress, args); break;
            case "rm":
                incorrectOperandsFunc(2, args);
                congress.remove(args[1]); break;
            case "log":
                logFunc(congress, args); break;
            case "global-log":
                incorrectOperandsFunc(1, args);
                congress.globalLog(); break;
            case "checkout":
                checkoutFunc(congress, args); break;
            case "status":
                incorrectOperandsFunc(1, args);
                congress.status(); break;
            case "find":
                incorrectOperandsFunc(2, args);
                congress.find(args[1]); break;
            case "branch":
                incorrectOperandsFunc(2, args);
                congress.branch(args[1]); break;
            case "rm-branch":
                incorrectOperandsFunc(2, args);
                congress.removeBranch(args[1]); break;
            case "reset":
                incorrectOperandsFunc(2, args);
                congress.reset(args[1]); break;
            case "merge":
                mergeFunc(congress, args); break;
            default:
                System.out.println("No command with that name exists.");
                break;
            }
            congress.saveToFile();
            System.exit(0);
        }
    }

    /** Peforms necessary checks for checkout and then calls the
     * necessary checkout using CONGRESS as commandCenter and ARGS
     * from input. */
    private static void checkoutFunc(CommandCenter congress, String... args) {
        if (args.length == 3 && args[1].equals("--")) {
            congress.checkoutOne(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            congress.checkoutTwo(args[1], args[3]);
        } else if (args.length == 2) {
            congress.checkoutThree(args[1], false);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Peforms checks for commit function and then performs it
     * using CONGRESS as CommandCenter and ARGS from input.
     */
    private static void commitFunc(CommandCenter congress, String... args) {
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else if (args.length == 1 || args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        congress.commit(args[1]);
    }

    /** Performs the check for incorrect Operands using A for number and ARGS
     * from input.     */
    private static void incorrectOperandsFunc(int a, String... args) {
        if (args.length != a) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /**Returns the commandCenter stored in file in the GITLET PATH.*/
    private static CommandCenter getCommandCenter() {
        File cmdctr = Utils.join(CWD, COMMANDCENTER_PATH);
        CommandCenter congress =
                Utils.readObject(cmdctr, CommandCenter.class);
        return congress;
    }

    /** Performs the necessary checks for merge and then calls the merge func
     * using CONGRESS and ARGS from input.
     */
    private static void mergeFunc(CommandCenter congress, String... args) {
        incorrectOperandsFunc(2, args);
        congress.merge(args[1]);
    }

    /** Performs necessary checks for log and then calls the log func
     * using CONGRESS and ARGS from input. */
    private static void logFunc(CommandCenter congress, String... args) {
        incorrectOperandsFunc(1, args);
        congress.log();
    }
}


