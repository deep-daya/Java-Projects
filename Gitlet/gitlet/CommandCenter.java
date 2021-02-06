package gitlet;


import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/** Command Center to implement the actions of Gitlet.
 *  @author Deep Dayaramani
 */
public class CommandCenter implements Serializable, Dumpable {
    /** Linked Hash Map to store the commit SHA (value) along with
     * their substrings (key).*/
    private LinkedHashMap<String, String> _commits;
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
    /** key: Branch Name value: Commits from init to branch. */
    private HashMap<String, ArrayList<String>> _branches;
    /** String to keep track of current head pointer. */
    private String _head;
    /** String to keep track of current active branch. */
    private String _branch;
    /** Staging Center. */
    private StagingCenter _house;
    /** key: (two branch names), value: commitID to track split). */
    private HashMap<LinkedHashSet<String>, String> splitPoints;

    /** Creates a new CommandCenter. */
    public CommandCenter() {
        _commits = new LinkedHashMap<>();
        _branches = new HashMap<>();
        _branch = "master";
        Commit init = new Commit("initial commit",
                new Date(0), null, new HashMap<String, String>());
        String shaInit = Utils.sha1(Utils.serialize(init));
        _head = shaInit;
        _commits.put(shaInit.substring(0, 5), shaInit);
        ArrayList<String> commitSHA = new ArrayList<>();
        commitSHA.add(shaInit);
        _branches.put("master", commitSHA);
        init.saveCommitToFile(shaInit);
        _house = new StagingCenter(_head);
        splitPoints = new HashMap<>();
    }

    /**Saves CommandCenter to a file.*/
    void saveToFile() {
        File gitFile = Utils.join(CWD, COMMANDCENTER_PATH);
        Utils.writeObject(gitFile, this);
    }

    /**Stages FILENAME for addition.*/
    void add(String fileName) {
        _house.add(fileName);
    }

    /**Initiates the commit function with COMMITMESSAGE
     * and adjusts the parameters in Command Center.*/
    void commit(String commitMessage) {
        Commit newCommit = _house.commit(commitMessage);
        String sha1Commit = Utils.sha1(Utils.serialize(newCommit));
        _commits.put(sha1Commit.substring(0, 5), sha1Commit);
        _house.setHead(sha1Commit);
        ArrayList<String> commitSHA = _branches.get(_branch);
        commitSHA.add(sha1Commit);
        _branches.put(_branch, commitSHA);
        newCommit.saveCommitToFile(sha1Commit);
        _head = sha1Commit;
        _house = new StagingCenter(_head);
    }

    /**Stages FILENAME for removal.*/
    void remove(String fileName) {
        _house.remove(fileName);
    }

    /**Performs the log function.*/
    void log() {
        String commit = _head;
        while (commit != null) {
            System.out.println("===");
            File commitF = Utils.join(CWD, COMMIT_PATH + commit);
            Commit current = Utils.readObject(commitF, Commit.class);
            System.out.printf("commit %s %n", commit);
            if (current.getMergeParent1() != null) {
                System.out.printf("Merge: %s %s %n",
                        current.getMergeParent1(), current.getMergeParent2());
            }
            DateFormat pstFormat =
                    new SimpleDateFormat("E MMM dd HH:mm:ss yyyy -0800");
            pstFormat.setTimeZone(TimeZone.getTimeZone("PST"));
            System.out.printf("Date: %s %n",
                    pstFormat.format(current.getDate()));
            System.out.println(current.getLogMessage());
            System.out.println();
            commit = current.getParent();
        }
    }

    /**Performs the globalLog function.*/
    void globalLog() {
        for (String commit: _commits.values()) {
            System.out.println("===");
            File commitF = Utils.join(CWD, COMMIT_PATH + commit);
            Commit current = Utils.readObject(commitF, Commit.class);
            System.out.printf("commit %s %n", commit);
            if (current.getMergeParent1() != null) {
                System.out.printf("Merge: %s %s %n",
                        current.getMergeParent1(), current.getMergeParent2());
            }
            DateFormat pstFormat =
                    new SimpleDateFormat("E MMM dd HH:mm:ss yyyy -0800");
            pstFormat.setTimeZone(TimeZone.getTimeZone("PST"));
            System.out.printf("Date: %s %n",
                    pstFormat.format(current.getDate()));
            System.out.println(current.getLogMessage());
            System.out.println();
        }
    }

    /**Checks out file with name FILENAME.*/
    void checkoutOne(String fileName) {
        File head = Utils.join(CWD, COMMIT_PATH + _head);
        Commit latest = Utils.readObject(head, Commit.class);
        String fileSHA = latest.shaFileInCommit(fileName);
        if (fileSHA == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        byte[] blobContents =
                Utils.readContents(Utils.join(CWD, BLOB_PATH + fileSHA));
        Utils.writeContents(Utils.join(CWD, fileName), blobContents);
    }

    /**Performs the second checkout for commit with
     * COMMITID and file FILENAME.*/
    void checkoutTwo(String commitID, String fileName) {
        if (!_commits.containsKey(commitID.substring(0, 5))) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            commitID = _commits.get(commitID.substring(0, 5));
        }

        File head = Utils.join(CWD, COMMIT_PATH + commitID);
        Commit latest = Utils.readObject(head, Commit.class);
        String fileSHA = latest.shaFileInCommit(fileName);
        if (fileSHA == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        byte[] blobContents =
                Utils.readContents(Utils.join(CWD, BLOB_PATH + fileSHA));
        Utils.writeContents(Utils.join(CWD, fileName), blobContents);
    }

    /**Performs the third checkout for a branch if ISRESET is false
     * and for commitID if RESET. Hence String is BRANCHORCOMMITID.*/
    void checkoutThree(String branchOrCommitID, boolean isReset) {
        String latestCommitSHA = "";
        if (!isReset) {
            if (_branches.get(branchOrCommitID) == null) {
                System.out.println("No such branch exists.");
                System.exit(0);
            }
            latestCommitSHA = _branches.get(branchOrCommitID).get(
                    _branches.get(branchOrCommitID).size() - 1);
            if (branchOrCommitID.equals(_branch)) {
                System.out.println("No need to"
                        + " checkout the current branch.");
                System.exit(0);
            }
        } else {
            if (!_commits.containsKey(branchOrCommitID.substring(0, 5))) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            } else {
                latestCommitSHA =
                        _commits.get(branchOrCommitID.substring(0, 5));
            }
        }
        List<String> currentFiles = Utils.plainFilenamesIn(CWD);
        Commit currentCommit =
                Utils.readObject(Utils.join(CWD,
                        COMMIT_PATH + _head), Commit.class);
        Commit incomingCommit =
                Utils.readObject(Utils.join(CWD,
                        COMMIT_PATH + latestCommitSHA), Commit.class);
        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        HashMap<String, String> newBlobs = incomingCommit.getBlobs();
        if (untrackedErrorFile(currentFiles, currentBlobs, newBlobs)) {
            return;
        }
        for (String fileName: newBlobs.keySet()) {
            File blobFile = Utils.join(CWD, BLOB_PATH + newBlobs.get(fileName));
            byte[] blobContent = Utils.readContents(blobFile);
            File toBeReplaced = Utils.join(CWD, fileName);
            Utils.writeContents(toBeReplaced, blobContent);
        }
        for (String fileName: currentFiles) {
            if (!newBlobs.keySet().contains(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        if (!isReset) {
            _branch = branchOrCommitID;
        } else {
            ArrayList<String> temp = _branches.get(_branch);
            temp.add(latestCommitSHA);
            _branches.put(_branch, temp);
        }
        _head = latestCommitSHA;
        _house = new StagingCenter(_head);
    }

    /** Finds commits with COMMITMESSAGE.*/
    void find(String commitMessage) {
        int count = 0;
        for (String commit : _commits.values()) {
            File commitF = Utils.join(CWD, COMMIT_PATH + commit);
            Commit current = Utils.readObject(commitF, Commit.class);
            if (current.getLogMessage().equals(commitMessage)) {
                System.out.println(commit);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**Provides the status of the CWD.*/
    void status() {
        System.out.println("=== Branches ===");
        List<String> branchNames = new ArrayList<String>(_branches.keySet());
        Collections.sort(branchNames);
        for (String branch: branchNames) {
            if (branch.equals(_branch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> stagedFiles = _house.stagedFiles();
        Collections.sort(stagedFiles);
        if (stagedFiles.size() != 0) {
            for (String fileName: stagedFiles) {
                System.out.println(fileName);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removedFiles = _house.removedFiles();
        Collections.sort(removedFiles);
        if (removedFiles.size() != 0) {
            for (String fileName: removedFiles) {
                System.out.println(fileName);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> modifiedFiles = new ArrayList<>();
        List<String> currentFiles = Utils.plainFilenamesIn(CWD);
        Commit currentCommit = Utils.readObject(
                Utils.join(CWD, COMMIT_PATH + _head), Commit.class);
        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        for (String fileName: currentBlobs.keySet()) {
            if (currentFiles.contains(fileName)
                    && fileCompare(fileName, currentBlobs.get(fileName))
                    && !stagedFiles.contains(fileName)) {
                modifiedFiles.add(fileName + " (modified)");
            } else if (!currentFiles.contains(fileName)
                    && !removedFiles.contains(fileName)) {
                modifiedFiles.add(fileName + " (deleted)");
            }
        }
        for (String fileName: stagedFiles) {
            if (currentFiles.contains(fileName)
                    && fileCompare(fileName,
                    Utils.sha1(_house.stagedFilesSHA(fileName)))) {
                modifiedFiles.add(fileName + " (modified)");
            } else if (!currentFiles.contains(fileName)) {
                modifiedFiles.add(fileName + " (deleted)");
            }
        }
        System.out.println();
        Collections.sort(modifiedFiles);
        statusUntracked(currentFiles,
                stagedFiles, currentBlobs, removedFiles);
    }

    /** Performs the untracked files version of status.
     *
     * @param currentFiles CWD Files
     * @param stagedFiles Current Staged Files
     * @param currentBlobs Current Blobs of Head
     * @param removedFiles Current Removed Files
     */
    private void statusUntracked(List<String> currentFiles,
                                 List<String> stagedFiles,
                                 HashMap<String, String> currentBlobs,
                                 List<String> removedFiles) {
        System.out.println("=== Untracked Files ===");
        ArrayList<String> untracked = new ArrayList<>();
        for (String fileName: currentFiles) {
            if (!currentBlobs.containsKey(fileName)
                    && !stagedFiles.contains(fileName)
                    && !removedFiles.contains(fileName)) {
                untracked.add(fileName);
            }
        }
        if (untracked.size() != 0) {
            Collections.sort(untracked);
            System.out.println(Arrays.toString(untracked.toArray()));
        }
        System.out.println();
    }

    /**Compares file contents for name FILENAME and other file SHA code as
     * CURRENTSHACOMMITID. Returns true if they aren't equal and false if
     * they are.*/
    private boolean fileCompare(String fileName, String currentSHAcommitID) {
        File currentWorkingFile = Utils.join(CWD, fileName);
        String currentSHAworkingFile =
                Utils.sha1(Utils.readContents(currentWorkingFile));
        return !currentSHAcommitID.equals(currentSHAworkingFile);
    }

    /**Creates a new branch with name BRANCHNAME.*/
    void branch(String branchName) {
        if (_branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        ArrayList<String> commitSHA = new ArrayList<>();
        commitSHA.add(_head);
        _branches.put(branchName, commitSHA);
        LinkedHashSet<String> temp = new LinkedHashSet<String>();
        temp.add(_branch);
        temp.add(branchName);
        splitPoints.put(temp, _head);
        for (String branchesNames: _branches.keySet()) {
            LinkedHashSet<String> tempo = new LinkedHashSet<String>();
            if (branchesNames.equals(_branch)) {
                continue;
            }
            tempo.add(branchesNames);
            tempo.add(_branch);
            String commitSplit = splitPoints.get(tempo);
            tempo.remove(_branch);
            tempo.add(branchName);
            splitPoints.put(tempo, commitSplit);
        }

    }
    /** Removes a branch with name BRANCHNAME. */
    void removeBranch(String branchName) {
        if (!_branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (_branch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        _branches.remove(branchName);
        ArrayList<LinkedHashSet<String>> temp =
                new ArrayList<>(splitPoints.keySet());
        for (LinkedHashSet<String> e: temp) {
            if (e.contains(branchName)) {
                splitPoints.remove(e);
            }
        }
    }
    /** Performs a reset function with Commit ID COMMITID. */
    void reset(String commitID) {
        checkoutThree(commitID, true);
    }

    /** Performs a merge operation for current branch and
     * branch with name BRANCHNAME.*/
    void merge(String branchName) {
        if (mergeErrorOut(branchName)) {
            return;
        }
        ArrayList<String> branchCommitList = _branches.get(branchName);
        String branchCommit = branchCommitList.get(branchCommitList.size() - 1);
        LinkedHashSet<String> temp = new LinkedHashSet<>();
        temp.add(_branch);
        temp.add(branchName);
        String ancestor = splitPoints.get(temp);
        if (mergeError2(branchCommit, _head, ancestor, branchName)) {
            return;
        }
        HashMap<String, String> branchBlob = mergeFileGetter(branchCommit);
        HashMap<String, String> splitBlob = mergeFileGetter(ancestor);
        HashMap<String, String> headBlob = mergeFileGetter(_head);
        boolean conflicting = false;
        List<String> currentFiles = Utils.plainFilenamesIn(CWD);
        if (untrackedErrorFile(currentFiles, headBlob, branchBlob)) {
            return;
        }
        for (String fileName: branchBlob.keySet()) {
            String headFileSHA = headBlob.get(fileName);
            String splitFileSHA = splitBlob.get(fileName);
            String branchFileSHA = branchBlob.get(fileName);
            if (splitFileSHA == null && headFileSHA == null) {
                mergeStager(branchFileSHA, fileName);
            } else if (headFileSHA == null
                    && branchFileSHA.equals(splitFileSHA)) {
                continue;
            } else if (headFileSHA.equals(splitFileSHA)
                    && !branchFileSHA.equals(splitFileSHA)) {
                mergeStager(branchFileSHA, fileName);
            } else if (branchFileSHA.equals(splitFileSHA)
                    && !headFileSHA.equals(splitFileSHA)) {
                continue;
            } else if (!branchFileSHA.equals(splitFileSHA)
                    && !headFileSHA.equals(splitFileSHA)
                    && branchFileSHA.equals(headFileSHA)) {
                continue;
            } else if (!branchFileSHA.equals(splitFileSHA)
                    && !headFileSHA.equals(branchFileSHA)
                    && !branchFileSHA.equals(headFileSHA)) {
                mergeHelper1(fileName, branchFileSHA, headFileSHA);
                conflicting = true;
            }
        }
        conflicting = mergeBlob(headBlob, splitBlob, branchBlob, conflicting);
        String logMessage = "Merged " + branchName + " into " + _branch + ".";
        mergeSplitPoints(branchCommit, branchName);
        mergeCommit(logMessage, branchCommit, _head);
        if (conflicting) {
            System.out.println("Encountered a merge conflict.");
            return;
        }
    }

    /**Returns true if conflicting is true and checks the blobs for
     * files in the headBlob, using HEADBLOB, SPLITBLOB, BRANCHBLOB and
     * adjusting CONFLICTING if there is a conflict.*/
    private boolean mergeBlob(HashMap<String, String> headBlob,
                                        HashMap<String, String> splitBlob,
                                        HashMap<String, String> branchBlob,
                                        boolean conflicting) {
        for (String fileName: headBlob.keySet()) {
            String headFileSHA = headBlob.get(fileName);
            String splitFileSHA = splitBlob.get(fileName);
            String branchFileSHA = branchBlob.get(fileName);
            if (splitFileSHA == null && branchFileSHA == null) {
                continue;
            } else if (splitFileSHA != null && branchFileSHA == null
                    && headFileSHA.equals(splitFileSHA)) {
                remove(fileName);
            } else if (splitFileSHA != null && branchFileSHA == null
                    && !headFileSHA.equals(splitFileSHA)) {
                mergeHelper1(fileName, null, headFileSHA);
                conflicting = true;
            }
        }
        return conflicting;
    }

    /** Reduces the length of merge by making changes to the splitPoints
     * after figuring out the merge. Uses BRANCHCOMMIT and BRANCHNAME to
     * adjust splitPoints. Makes the branchCommit the split point of the
     * current branch and branchName as well as for those branches which
     * have the current branchCommit as their commitID.
     */
    private void mergeSplitPoints(String branchCommit, String branchName) {
        if (splitPoints.containsValue(branchCommit)) {
            ArrayList<LinkedHashSet<String>> temporary =
                    new ArrayList<>(splitPoints.keySet());
            for (LinkedHashSet<String> e: temporary) {
                if (splitPoints.get(e).equals(branchCommit)) {
                    if (e.contains(branchName)) {
                        e.remove(branchName);
                        e.add(_branch);
                        splitPoints.replace(e, branchCommit);
                    }
                }
            }
        }
        LinkedHashSet<String> tempor = new LinkedHashSet<>();
        tempor.add(_branch);
        tempor.add(branchName);
        splitPoints.put(tempor, branchCommit);
    }
    /** Reduces the length of merge by Staging files using
     *  BRANCHFILESHA and FILENAME.
     */
    private void mergeStager(String branchFileSHA, String fileName) {
        File tempBlob = Utils.join(CWD, BLOB_PATH + branchFileSHA);
        File tempFile = Utils.join(CWD, fileName);
        Utils.writeContents(tempFile, Utils.readContents(tempBlob));
        add(fileName);
    }

    /** Returns true if Files are untracked or false if not.
     *
     * @param currentFiles files in CWD
     * @param currentBlob Current Head Blob
     * @param newBlob Incoming Blob
     */
    private boolean untrackedErrorFile(List<String> currentFiles,
                                       HashMap<String, String> currentBlob,
                                    HashMap<String, String> newBlob) {
        ArrayList<String> untracked = new ArrayList<>();
        for (String fileName: currentFiles) {
            if (!currentBlob.containsKey(fileName)
                    && newBlob.containsKey(fileName)) {
                untracked.add(fileName);
            }
        }
        if (untracked.size() != 0) {
            System.out.println("There is an untracked file "
                   + "in the way; delete it, or add and commit it first.");
            return true;
        }
        return false;
    }
    /**Helper function for merge Function, which writes
     * file with name FILENAME with content from HEADFILESHA and
     * BRANCHFILESHA in a specific format. */
    private void mergeHelper1(String fileName,
                              String branchFileSHA, String headFileSHA) {
        File tempFile = new File(fileName);
        String blobHead;
        String blobBranch;
        if (headFileSHA == null) {
            blobHead = "";
        } else {
            blobHead = Utils.readContentsAsString(
                    Utils.join(CWD, BLOB_PATH + headFileSHA));
        }
        if (branchFileSHA == null) {
            blobBranch = "";
        } else {
            blobBranch = Utils.readContentsAsString(
                    Utils.join(CWD, BLOB_PATH + branchFileSHA));
        }
        Utils.writeContents(tempFile, "<<<<<<< HEAD\n"
                + blobHead + "=======\n"
                + blobBranch + ">>>>>>>\n");
        add(fileName);
    }

    /** Performs checks for errors before continuing merge using B
     * BRANCHNAME and returns true if there is an error and false
     * if not.*/
    private boolean mergeErrorOut(String branchName) {
        if (!_house.unStaged()) {
            System.out.println("You have uncommitted changes.");
            return true;
        } else if (!_branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return true;
        } else if (branchName.equals(_branch)) {
            System.out.println("Cannot merge branch with itself.");
            return true;
        }
        return false;
    }

    /** Reduces the length of merge by checking for errors in this function
     * using BRANCHSHA, HEADSHA, SPLITSHA and the BRANCHNAME in case of
     * checking out the branch. Returns true if there is an error and false
     * if not.
     */
    private boolean mergeError2(String branchSHA, String headSHA,
                                String splitSHA, String branchName) {
        if (branchSHA.equals(splitSHA)) {
            System.out.println("Given branch"
                    + " is an ancestor of the current branch.");
            return true;
        }
        if (headSHA.equals(splitSHA)) {
            checkoutThree(branchName, false);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }
        return false;
    }
    /** Returns the Blobs for a HEADCOMMIT ID.. */
    private HashMap<String, String> mergeFileGetter(String headCommit) {
        Commit head = Utils.readObject(
                Utils.join(CWD, COMMIT_PATH + headCommit), Commit.class);

        return head.getBlobs();
    }
    /**Performs mergeCommit function with Message COMMITMESSAGE,
     * BRANCHCOMMIT, HEADCOMMIT. */
    private void mergeCommit(String commitMessage,
                             String branchCommit, String headCommit) {
        String mergeParent1 = branchCommit.substring(0, 7);
        String mergeParent2 = headCommit.substring(0, 7);
        Commit commitMerge = _house.mergeCommit(
                commitMessage, mergeParent1, mergeParent2);
        String sha1Commit = Utils.sha1(Utils.serialize(commitMerge));
        _commits.put(sha1Commit.substring(0, 5), sha1Commit);
        ArrayList<String> commitSHA = _branches.get(_branch);
        commitSHA.add(sha1Commit);
        _branches.put(_branch, commitSHA);
        commitMerge.saveCommitToFile(sha1Commit);
        _head = sha1Commit;
        _house = new StagingCenter(_head);
    }
    @Override
    public void dump() {
        System.out.printf("_commitSize:%d %n, _head :"
                + "%s %n, _branch: %s %n", _commits.size(), _head, _branch);
    }



}
