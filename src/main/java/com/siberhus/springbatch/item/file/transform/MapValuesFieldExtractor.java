package com.siberhus.springbatch.item.file.transform;

import java.util.Map;

import org.springframework.batch.item.file.transform.FieldExtractor;

public class MapValuesFieldExtractor implements FieldExtractor<Map<String, Object>>{
	
	@Override
	public Object[] extract(Map<String, Object> item) {
		
		return item.values().toArray();
	}
	
}
