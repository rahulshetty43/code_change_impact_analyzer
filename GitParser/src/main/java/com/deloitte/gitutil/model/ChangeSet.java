package com.deloitte.gitutil.model;

import java.io.Serializable;

public class ChangeSet   implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	String className;
	String methodName;
	String projectName;
	String packageName;
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public ChangeSet() {
		
	}
	
	public ChangeSet(String className, String projectName,
			String packageName, String methodName) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.projectName = projectName;
		this.packageName = packageName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	@Override
	public String toString() {
		return projectName+","+packageName+","+className+","+methodName+"\n";
		
	}
	
	
	
	
}