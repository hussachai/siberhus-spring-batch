package com.siberhus.springbatch.item;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Messages {

	private Set<String> messages = new LinkedHashSet<String>();
	
	public boolean add(String msg){
		return messages.add(msg);
	}
	
	public boolean add(String fieldName, String msg){
		return messages.add(fieldName+": "+msg);
	}
	
	public Iterator<String> iterator(){
		return messages.iterator();
	}
	
	public int size(){
		return messages.size();
	}
	
	public void clear(){
		this.messages.clear();
	}
	
	public String toString(){
		if(messages.isEmpty()){
			return "";
		}
		return messages.toString();
	}
}
