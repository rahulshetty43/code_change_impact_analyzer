# Overview
This utility can find all the root callers of a method which was modified recently based on the git commit history and can do sanity check on those root callers

## Prerequisites
 - [Java SE 1.8 or above](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
 - [Eclipse Mars or above](https://www.eclipse.org/)
 
## About the utility
Currently utility has two different projects GitParser and CallingMemberAnalyzer, GitParser is to identify recently modifed methods from git commits and 
CallingMemberAnalyzer is to find all the root callers of a modified method.

## Setting up in Eclipse 
Setup is required only to make modifications to the project, if you are not planning to do any modification, skip this step.

### Setting up GitParser
This is a maven project and can be direclty imported into eclipse  [File -> Import -> Exsisting maven project].
This project requires two mandatory and one optional parameter from user which can be supplied thorugh config.properties file.

    > gitRepo=C:/Users/prsekar/dcs_backend/.git
    
    > storage=../storage/
    
    > timeDurationInHours=24

property "gitRepo" points to git repository to be analyzed, property "storage" points to temporary storage required by java process while executing this porject,
third property "timeDurationInHours" is an optional property which defaults to 24hr if not provided by user. this property indicates timeframe of the commits 
which has to be considred.



### Setting up CallingMemberAnalyzer
This is a eclipse plugin project and should be imported as existing eclipse project [File -> Import -> Exsiting Project Into Workspace].
This project requires path to the ecplise workpsace in which git project is imported, path to the changeSet.csv file which was created by GitParser and
path to mails.properties file which will have smtp host, sender and receiver details.


## Running the Utility from eclipse

- GitParser can be triggerd as  "Run as ->  Java application" at the end of the execution it generates changeSet.csv file in "storage" location which will have  
    details of all the modified methods in specified time frame (last 24 hrs if timeDurationInHours is not specified)

- CallingMemberAnalyzer is an eclipse plugin project and should be Run as - > Eclipse Application from within the eclipse,but it requires couple of parameters to 
    be passed as by user from as Arguments, so first time it has to triggerd from Run as - > Run configuration, input the eclipse workspace where code is present, 
    goto program arguments in Arguments tab and input the file path to changeSet.csv file and path to mails.properties file.


## Exporting as Jar

- GitParser can be exported as runnabel jar from eclipse
- CallingMemberAnalyzer should be exported as Deployable plug-ins and fragments which will create a jar file in dropins folder of eclipse

## Running from command line

- Once you have both the jars, plugin jar should be installed in eclipse which will enable the option to execute plugin from command line.
- Place the plugin jar in dropins folder of eclipse if not present already
- Restart the eclipse, this will deploy the plugin jar 
- Run the git-parser jar, config.properties file should be present in the same folder as the jar 
    
    ```     
     >java -jar git.parser.jar
     
     >eclipsec.exe  -data C:\Users\prsekar\Desktop\cf\new_code\ -nosplash -application HeadlessAnalyzer.myhead\
     C:\\Users\\prsekar\\Desktop\\cf\\Disney\\storage\\changeSet.csv\
     C:\\Users\\prsekar\\Desktop\\cf\\Disney\\Headless\\mails.properties
     
    ```   
- Here -data argument represents the workspace of the eclipse project and last two arguments represents location changeSet.csv file and mails.properies file.

Once utility is successfully execute, a mail will be send out with modified methods and root callers of thoese methods.
    
    
