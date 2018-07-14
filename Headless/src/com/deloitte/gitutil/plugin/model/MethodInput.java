package com.deloitte.gitutil.plugin.model;

public class MethodInput {
	String projectName;
	String packageName;
	String className;
	String methodName;

	public MethodInput(String projectName,String packageName,String className,String methodName) {
		this.projectName=projectName;
		this.packageName=packageName;
		this.className=className;
		this.methodName=methodName;
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
	 String getmethodName(){
		 return this.methodName;
	 }
}
