# Gitlet Design Document

**Name**: Deep Dayaramani

## Classes and Data Structures

####GitletException:
General exception indicating a Gitlet error.  For fatal errors, the
result of .getMessage() is the error message to be printed.

####DumpObj
A class which reads files, deserializes it and calls dump on each file.
####Dumpable:
Interface for the DumpObj and controls debugging for serialization.

####Commit
Data Structure controlling commits and storing log information. 
Implements Dumpable and Serializable.
1. String logMessage: commit message
2. Date _timeStamp: time stamp for commit
3. String parentCommit: SHA Code of parent commit
4. Commit parent: parent commit
5. String mergeParentUn: SHA Code of merge Parent 1.
6. String mergeParentDeux: SHA Code of merge Parent 2.
7. Some sort of map for tracking blobs and SHA codes.


####Main:
Basically a function caller for Command Center. Tells which command to call in command center.
I would call this the Head of DOD telling orders to the command center, received by
the President, aka you the user.
####Utils
Assorted utilities. Helps with SHA and stuff. 

####CommandCenter
Another object which controls a high level overview of all the other objects to call.
1. Some sort of map to track commits.
2. Some sort of map to track branch.
3. Strings to track directories.
4. A staging area.
5. Current head and branch. 
####StagingCenter:
This will be the staging center for files which are added. Might be an object which implements Serializable and Dumpable. 

1. Commit head: Current head commit.
2. Some sort of map: Files to be committed.
3. Some sort of map: Files to be removed.

####Unit Test
Testing class used to test simple functions within other classes.




## Algorithms
####Main Class:
Basically a command is called into Main, which checks for errors listed in the spec
and then sends it to the command center. Contains all the commands listed in the spec
and checks which exact one is the one to call. 

####Command Center: 
Stores all the data necesarry. Commit History, branch history, merge history,
current head, current branch, calls files to be added in its staging center.
Saves commits. Checks for conflicts, calls on functions in the staging center.


####Staging Center: 
A sublevel center which contains the real add function, commit function,
merging function, remove function, saves blobs to directory. Does the dirty work for the Command Center.

## Persistence
1. Write down the Commit SHAs to the disk and the Blob SHAs to the disk.
We can serialize them into bytes and store in a file on the disk. Use writeObject method. Use this
while committing, and checking out I guess.


In order to retrieve our state, before executing any code, we need to search for the saved files in the working directory (folder in which our program exists) and load the objects that we saved in them

We can use the readObject method from the Utils class to read the data of files as and deserialize the objects we previously wrote to these files.
File names are probably going to be SHA codes.
