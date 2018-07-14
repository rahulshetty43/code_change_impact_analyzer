package com.deloitte.gitutil.plugin.model;

public class ClassInput {
	String projectName;
	String packageName;
	String className;


	public ClassInput(String projectName,String packageName,String className) {
		this.projectName=projectName;
		this.packageName=packageName;
		this.className=className;
	}
	 String getProjectName(){
		 return this.projectName;
	 }
	 String getPackageName(){
		 return this.packageName;
	 }
	 String getclassName(){
		 return this.className;
	 }
}
