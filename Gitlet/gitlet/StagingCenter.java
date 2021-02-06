package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Date;

/** Staging Center class for Gitlet.
 *  @author Deep Dayaramani
 */
public class StagingCenter implements Serializable, Dumpable {

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

    /**Array List to Store fileNames staged to be removed.*/
    private ArrayList<String> _tbRemove;
    /**key: fileName value: Serialized file content.*/
    private HashMap<String, byte[]> _tbCommitted;
    /**Stores the current head.*/
    private String _head;

    /** Creates a new StagingCenter with head- HEAD. */
    public StagingCenter(String head) {
        _tbCommitted = new HashMap<>();
        _tbRemove = new ArrayList<>();
        _head = head;
    }

    /**@return if any files are staged or not.*/
    boolean unStaged() {
        return _tbCommitted.size() == 0 && _tbRemove.size() == 0;
    }

    /**@return staged files for addition.*/
    ArrayList<String> stagedFiles() {
        return new ArrayList<>(_tbCommitted.keySet());
    }

    /**@return files staged to be removed.*/
    ArrayList<String> removedFiles() {
        return _tbRemove;
    }

    /**@return Serialized content of files staged for addition
     * with fileName KEY.*/
    byte[] stagedFilesSHA(String key) {
        return _tbCommitted.get(key);
    }

    /**Adds the files to staging center with name FILENAME.*/
    void add(String fileName) {
        File addFile = Utils.join(CWD, fileName);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        byte[] contentsAddFile = Utils.readContents(addFile);
        String shaFiletbAdded = Utils.sha1(contentsAddFile);
        Commit parent = Utils.readObject(
                Utils.join(CWD, COMMIT_PATH + _head), Commit.class);
        String shaFileExist = parent.shaFileInCommit(fileName);
        if (shaFileExist == null || !shaFiletbAdded.equals(shaFileExist)) {
            _tbCommitted.put(fileName, contentsAddFile);
        } else {
            _tbCommitted.remove(fileName);
        }
        _tbRemove.remove(fileName);
    }

    /**Sets head of the staging center to CURHEAD.*/
    void setHead(String curHead) {
        _head = curHead;
    }
    /**@return number of files staged for addition.*/
    Integer getStagedLength() {
        return _tbCommitted.size();
    }
    /**@return number of files staged for removal.*/
    Integer getRemLength() {
        return _tbRemove.size();
    }

    /**Performs the commit function with the message COMMITMESSAGE
     * and returns the new Commit.*/
    Commit commit(String commitMessage) {
        if (unStaged()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit parent = Utils.readObject(
                Utils.join(CWD, COMMIT_PATH + _head), Commit.class);
        HashMap<String, String> blobsMaps = new HashMap<>();
        for (String fileName: _tbCommitted.keySet()) {
            String shaFile = Utils.sha1(_tbCommitted.get(fileName));
            blobsMaps.put(fileName, shaFile);
            saveBlobToFile(shaFile, _tbCommitted.get(fileName));
        }
        HashMap<String, String> parentBlobs = parent.getBlobs();
        for (String fileName: parentBlobs.keySet()) {
            if (!blobsMaps.containsKey(fileName)) {
                blobsMaps.put(fileName, parentBlobs.get(fileName));
            }
        }
        for (String fileName: _tbRemove) {
            blobsMaps.remove(fileName);
        }
        String parentSHA = _head;
        Commit answer =
                new Commit(commitMessage, new Date(), parentSHA, blobsMaps);
        _head = Utils.sha1(Utils.serialize(answer));
        return answer;
    }

    /**Stages the file with name FILENAME for removal.*/
    void remove(String fileName) {
        boolean changeMade = false;
        if (_tbCommitted.size() != 0) {
            if (_tbCommitted.keySet().contains(fileName)) {
                _tbCommitted.remove(fileName);
                changeMade = true;
            }
        }
        Commit parent = Utils.readObject(
                Utils.join(CWD, COMMIT_PATH + _head), Commit.class);
        HashMap<String, String> blobsCommit = parent.getBlobs();
        if (blobsCommit.containsKey(fileName)) {
            _tbRemove.add(fileName);
            File doYouExist = Utils.join(CWD, fileName);
            if (doYouExist.exists()) {
                doYouExist.delete();
            }
        } else if (!changeMade) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /** Saves the blob to file in BLOB_PATH with shacode SHABLOB, and
     * byte[] BLOB.*/
    private void saveBlobToFile(String shaBlob, byte[] blob) {
        File blobFile = Utils.join(CWD, BLOB_PATH + shaBlob);
        Utils.writeContents(blobFile, blob);
    }

    /** Performs the mergeCommit with MESSAGE, MERGEPARENT1,
     * PARENT2 to return a commit. */
    Commit mergeCommit(String message, String mergeParent1, String parent2) {
        if (unStaged()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit parent = Utils.readObject(
                Utils.join(CWD, COMMIT_PATH + _head), Commit.class);
        HashMap<String, String> blobsMaps = new HashMap<>();
        for (String fileName: _tbCommitted.keySet()) {
            String shaFile = Utils.sha1(_tbCommitted.get(fileName));
            blobsMaps.put(fileName, shaFile);
            saveBlobToFile(shaFile, _tbCommitted.get(fileName));
        }
        HashMap<String, String> parentBlobs = parent.getBlobs();
        for (String fileName: parentBlobs.keySet()) {
            if (!blobsMaps.containsKey(fileName)) {
                blobsMaps.put(fileName, parentBlobs.get(fileName));
            }
        }
        for (String fileName: _tbRemove) {
            blobsMaps.remove(fileName);
        }
        String parentSHA = _head;
        Commit answer = new Commit(message, new Date(), parentSHA, blobsMaps);
        answer.setMergeParent1(mergeParent1);
        answer.setMergeParent2(parent2);
        _head = Utils.sha1(Utils.serialize(answer));
        return answer;
    }
    @Override
    public void dump() {
        System.out.printf(", staged_commit_size:%d %n, "
                + "staged_removed_size:%d %n",
                getStagedLength(), getRemLength());
    }

}
