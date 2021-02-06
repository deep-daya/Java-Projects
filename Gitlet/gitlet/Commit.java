package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;


/** Class to represent a commit and all it's parts.
 * @author Deep Dayaramani*/
public class Commit implements Serializable, Dumpable {
    /**String to store Commit log message. */
    private String _logMessage;
    /** Date of commit. */
    private Date _datetime;
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
    /** String to store merge parent in case of merge commit. */
    private String _mergeParent1 = null;
    /** String to store 2nd merge parent. */
    private String _mergeParent2 = null;
    /** String to store parent SHA commit. */
    private String _parent;
    /**key: fileName, value: SHA Code. */
    private HashMap<String, String> _blobs;

    /**Creates a new Commit with message LOGMESSAGE, date DATETIME,
     * parent PARENT, and blobmap BLOBSMAP. */
    public Commit(String logMessage, Date datetime,
                  String parent, HashMap<String, String> blobsMap) {
        _logMessage = logMessage;
        _datetime = datetime;
        _parent = parent;
        _blobs = new HashMap<>();
        _blobs.putAll(blobsMap);
    }

    /**Saves the Commit to a file with name SHA inside COMMIT_PATH. */
    void saveCommitToFile(String sha) {
        File commitFile = Utils.join(CWD, COMMIT_PATH + sha);
        Utils.writeObject(commitFile, this);
    }

    /** Checks if FILENAME is in blobMap of commit.
     * to return the BlobSHA */
    String shaFileInCommit(String filename) {
        if (_blobs.containsKey(filename)) {
            return _blobs.get(filename);
        }
        return null;
    }

    /** @return the BlobMap of the current commit. */
    HashMap<String, String> getBlobs() {
        return _blobs;
    }

    /** @return  the parent SHA code. */
    String getParent() {
        return _parent;
    }

    /** @return the Date of the Commit. */
    Date getDate() {
        return _datetime;
    }

    /** @return Log message. */
    String getLogMessage() {
        return _logMessage;
    }
    /** Sets merge parent 1 to MERGEPARENT1.*/
    void setMergeParent1(String mergeParent1) {
        _mergeParent1 = mergeParent1;
    }
    /** Sets merge parent 2 to MERGEPARENT2. */
    void setMergeParent2(String mergeParent2) {
        _mergeParent2 = mergeParent2;
    }
    /** @return Merge Parent 1. */
    String getMergeParent1() {
        return _mergeParent1;
    }
    /** @return Merge Parent 2. */
    String getMergeParent2() {
        return _mergeParent2;
    }

    @Override
    public void dump() {
        System.out.printf("LogMessage: %s %n, "
                        + "_blobsLength: %d %n, _parent: %s %n, "
                        + "_blobs_key_set:%s %n", _logMessage,
                _blobs.size(), _parent, _blobs.keySet().toString());
    }

}
