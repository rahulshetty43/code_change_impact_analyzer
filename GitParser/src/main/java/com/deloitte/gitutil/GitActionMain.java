package com.deloitte.gitutil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import com.deloitte.gitutil.model.ChangeSet;

public class GitActionMain {
	
	public static Date dt;
	public static HashMap<String, Integer> h1 = new HashMap();
	public static HashMap<String, String> h2 = new HashMap();

	
	
	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {
		
		if(args.length > 0 ) {
			System.out.println("loading config file from ::" +args[0]);
			ConfigProvider.init(args[0]);
		}else {
			System.out.println("loading config file classpath");
			ConfigProvider.init("config.properties");
		}
		
		ConfigProvider.init("config.properties");
		
		HashMap<String, ArrayList<ChangeSet>> h3 = new HashMap();
		ArrayList<ChangeSet> changeList = new ArrayList<ChangeSet>();
		
		dt = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.add(Calendar.HOUR, ConfigProvider.timeDuration*(-1));
		dt = cal.getTime();
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		Repository repository = repositoryBuilder.setGitDir(new File(ConfigProvider.gitRepoPaht))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();
		System.out.println("Repository Opened");
		Git git = new Git(repository);
		System.out.println("Git Object Initiated");
		
		ObjectId head = repository.resolve(Constants.HEAD);
		
		//Find modified files in the given duration
		commitHistory(git, head, repository);
		
		//Store files inside a folder
		readRevFile(repository);
		MethodDiff md = new MethodDiff();
		
		//Get modified methods for already present files
		for (String s: h1.keySet()) {
			if (s.endsWith(".java") && h1.get(s) == 1) {
				changeList.addAll(md.methodDiffInClass(
						ConfigProvider.prevRevPath + s,
						ConfigProvider.headRevPath + s));
				if(!h3.containsKey(s)) {
					h3.put(s, changeList);
					System.out.println(s+ "  "+h3.get(s));
				}
			}
		}
		if (!h3.isEmpty()) {
			System.out.println(changeList.size());
			System.out.println("Done finding modified methods");
			write(changeList);
		}
		else {
			System.out.println("Either only new files were added or no '.java' files were modified");
		}
		
		//Get all methods for newly added files
		
	}

	
	
	
	//Read contents of a file from older commit
	public static void readRevFile(Repository repository) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		
		for (String s: h2.keySet()) {
			if (h1.get(s) == 1) {
				
				ObjectId pastCom = repository.resolve(h2.get(s));
				ObjectId headCom = repository.resolve(Constants.HEAD);
				RevWalk rw = new RevWalk(repository);
				RevCommit rcOld = rw.parseCommit(pastCom);
				RevCommit rcNew = rw.parseCommit(headCom);
				RevTree rtOld = rcOld.getTree();
				RevTree rtNew = rcNew.getTree();
				System.out.println();

				
				try (TreeWalk treeWalkOld = new TreeWalk(repository)) {
                    treeWalkOld.addTree(rtOld);
                    treeWalkOld.setRecursive(true);

                    treeWalkOld.setFilter(PathFilter.create(s));
                    if (!treeWalkOld.next()) {
                        throw new IllegalStateException("Did not find expected file " + s);
                    }
                    
                    ObjectId objectId = treeWalkOld.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    
                    
                    Path path = Paths.get(ConfigProvider.headRevPath+s);
                    Files.createDirectories(path.getParent());                                       
                                      
                    File f = new File(ConfigProvider.headRevPath+s);                    
                    if(f.exists()) {
                    	f.delete();
                    }
                    f.createNewFile();
                                        
                    // and then one can the loader to read the file
                    OutputStream outp = new FileOutputStream(f);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
						loader.copyTo(out);
					} catch (Exception e) {
						e.printStackTrace();
					}
                    out.writeTo(outp);
                }
				System.out.println("PastRev File Downloaded");
				
				try (TreeWalk treeWalkHead = new TreeWalk(repository)) {
                    treeWalkHead.addTree(rtNew);
                    treeWalkHead.setRecursive(true);
                    treeWalkHead.setFilter(PathFilter.create(s));
                    if (!treeWalkHead.next()) {
                        throw new IllegalStateException("Did not find expected file " + s);
                    }
                    
                    ObjectId objectId = treeWalkHead.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);
                    
                    
                    Path path = Paths.get(ConfigProvider.prevRevPath+s);
                    Files.createDirectories(path.getParent());                                       
                                      
                    File f = new File(ConfigProvider.prevRevPath+s);                    
                    if(f.exists()) {
                    	f.delete();
                    }
                    f.createNewFile();
                                        
                    // and then one can the loader to read the file
                    OutputStream outp = new FileOutputStream(f);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
						loader.copyTo(out);
					} catch (Exception e) {
						e.printStackTrace();
					}
                    out.writeTo(outp);
                }
				System.out.println("Head Rev File downloaded");
			}
		}
		
	}
	
	
	//Will return the files changed against commit IDs
	public static void commitHistory(Git git, ObjectId head, Repository repository) throws NoHeadException, GitAPIException, IncorrectObjectTypeException, CorruptObjectException, IOException 
	{	
		
	    Iterable<RevCommit> logs = git.log().call();
	    int k = 0;
	    for (RevCommit commit : logs) {
	        String commitID = commit.getName();
	        if (commitID != null && !commitID.isEmpty())
	        {
	            TreeWalk tw = new TreeWalk(repository);
	            tw.setRecursive(true);
	            RevCommit commitToCheck = commit;
	            tw.addTree(commitToCheck.getTree());
	            for (RevCommit parent : commitToCheck.getParents())
	            {
	                tw.addTree(parent.getTree());
	            }
	            while (tw.next())
	            {
	                int similarParents = 0;
	                for (int i = 1; i < tw.getTreeCount(); i++)
	                    if (tw.getFileMode(i) == tw.getFileMode(0) && tw.getObjectId(0).equals(tw.getObjectId(i)))
	                        similarParents++;
	           
	                if ((new Date(commit.getCommitTime() * 1000L)).after(dt) && !h1.containsKey(tw.getPathString()) && similarParents==0) {
						h1.put(tw.getPathString(), 0);

						System.out.println("Revision: "+ commitID+ "  Time: "+ (new Date(commit.getCommitTime() * 1000L))+ "  File names: "+ tw.getNameString() + "  "+ tw.getPathString());
					
					}
	                
	            }
	        }
	    }

	    System.out.println();
	    Date da = new Date();
	    Calendar caa = Calendar.getInstance();
	    caa.setTime(da);
	    caa.add(Calendar.MINUTE, 60);
	    da = caa.getTime();
	    
	    for(String s: h1.keySet()) {
	    	for (RevCommit rc: git.log().add(head).addPath(s).call()) {
	    		if ((new Date(rc.getCommitTime() * 1000L)).before(dt)) {
	    			h2.put(s, rc.getName());
	    			h1.put(s, h1.get(s)+1);
	    			break;
	    		}
	    		System.out.println("Path: "+ s + " Time: "+ (new Date(rc.getCommitTime() * 1000L)));
	    	}
	    }
	    
	    System.out.println();
	    for(String s: h2.keySet()) {
	    	System.out.println("Path: "+ s + " Revision: "+ h2.get(s));
	    }
	    
	}
	public static void write(ArrayList<ChangeSet> changeSet) throws IOException {
		try
		{
			PrintWriter pw = new PrintWriter(ConfigProvider.storagePath+"changeSet.csv");
			pw.flush();	
			pw.append(ConfigProvider.CSV_HEADER_PROJECT_NAME+","+ConfigProvider.CSV_HEADER_PACKAGE_NAME+","+ConfigProvider.CSV_HEADER_CLASS_NAME+","+ConfigProvider.CSV_HEADER_METHOD_NAME+"\n");
			for(ChangeSet cs : changeSet) {
				System.out.println(cs);
				pw.append(cs.toString());
			}
			            
			pw.close();
			System.out.println("Serialized data is saved in changeSet.csv");

		}catch(IOException i)
		{
			i.printStackTrace();
			throw i;
		}

	}
	
}
