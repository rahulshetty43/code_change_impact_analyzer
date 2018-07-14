# Overview
This utility can find all the root callers of a method which was modified recently based on the git commit history and can do sanity check on those root callers

## Prerequisites
 - [Java SE 1.8 or above](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
 - [Eclipse Mars or above](https://www.eclipse.org/)
 
## About the utility
Currently utility has two different projects GitParser and CallingMemberAnalyzer, GitParser is to identify recently modifed methods from git commits and 
CallingMemberAnalyzer is to find all the root callers of a modified method.

## Setup
Setup is required only to make modifications to the project, if you are not planning to do any modification, skip this step.

### Setting up GitParser
This is a maven project and can be direclty imported into eclipse  [File -> Import -> Exsisting maven project]

### Setting up CallingMemberAnalyzer
This is a eclipse plugin project and should be imported as existing eclipse project [File -> Import -> Exsiting Project Into Workspace]


