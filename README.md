# Overview
This utility can find all the root callers of a method which was modified recently based on the git commit history and can do sanity check on those root callers

## Prerequisites
 - [Java SE 1.8 or above](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
 - [Eclipse Mars or above](https://www.eclipse.org/)
 
## About the utility
    Currently utility has two different projects GitParser and CallingMemberAnalyzer, GitParser is to identify recently modifed methods from git commits and 
CallingMemberAnalyzer is to find all the root callers of a modified method.
