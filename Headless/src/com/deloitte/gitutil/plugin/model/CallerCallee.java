package com.deloitte.gitutil.plugin.model;

import java.io.Serializable;

public class CallerCallee implements Serializable{
	private static final long serialVersionUID = 1L;
	String Caller;
	String Callee;
	public CallerCallee() {
	}
	public CallerCallee(String callee,String caller){
		this.Callee=callee;
		this.Caller=caller;
	}
	String getCaller(){
		return this.Caller;
	}

	void setCaller(String caller){
		this.Caller=caller;
	}
	String getCallee(){
		
		return this.Callee;
	}
	void setCallee(String callee){
		this.Callee=callee;
	}
	
	@Override
	public String toString() {
		return Caller+","+Callee+"\n";
	}
	
	
}
