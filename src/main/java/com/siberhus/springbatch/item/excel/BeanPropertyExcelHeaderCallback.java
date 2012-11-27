package com.siberhus.springbatch.item.excel;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class BeanPropertyExcelHeaderCallback<T> implements ExcelHeaderCallback, BeanFactoryAware, InitializingBean {

	private BeanFactory beanFactory;
	
	private String name;
	
	private Class<? extends T> type;
	
	@Override
	public void writeHeader(Row row) {
		
		Class<?> beanClass = null;	
		if(name!=null){
			beanClass = beanFactory.getBean(name).getClass();
		}else{
			beanClass = type;
		}
		
		List<String> getterNames = new ArrayList<String>();
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(beanClass);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
		PropertyDescriptor propDescs[] = beanInfo.getPropertyDescriptors();
		for(PropertyDescriptor propDesc: propDescs){
			if(propDesc.getReadMethod()!=null){
				getterNames.add(propDesc.getName());
			}
		}
		for(int i=0;i<getterNames.size();i++){
			Cell cell = row.createCell(i);
			cell.setCellValue(getterNames.get(i));
		}
	}
	
	/**
	 * 
	 * Either this property or the type property must be specified, but not
	 * both.
	 * 
	 * @param name the name of a prototype bean in the enclosing BeanFactory
	 */
	public void setBeanName(String name) {
		this.name = name;
	}
	
	/**
	 * Either this property or the prototype bean name must be specified, but
	 * not both.
	 * 
	 * @param type the type to set
	 */
	public void setTargetType(Class<? extends T> type) {
		this.type = type;
	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	/**
	 * Check that precisely one of type or prototype bean name is specified.
	 * 
	 * @throws IllegalStateException if neither is set or both properties are
	 * set.
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(name != null || type != null, "Either name or type must be provided.");
		Assert.state(name == null || type == null, "Both name and type cannot be specified together.");
		
		
		
	}

	
	
	
}
