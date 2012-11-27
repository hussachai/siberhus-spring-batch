package com.siberhus.springbatch.item.file.mapping;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.beanutils.ConversionException;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import com.siberhus.springbatch.item.ErrorMessageable;
import com.siberhus.springbatch.item.WarningMessageable;

/**
 * 
 * @author hussachai
 *
 * @param <T>
 */
public class CommonsBeanWrapFieldSetMapper<T> implements FieldSetMapper<T>, BeanFactoryAware, InitializingBean{
	
	protected String name;
	
	protected Class<? extends T> type;

	protected BeanFactory beanFactory;
	
	private static Map<Class<?>, Map<String, String>> propertiesMatched = new HashMap<Class<?>, Map<String, String>>();
	
	protected static Map<Class<?>, String[]> propertyNamesCache = new HashMap<Class<?>, String[]>();
	
	private static int distanceLimit = 5;
	
	protected boolean strict = false;
	
	@Override
	public T mapFieldSet(FieldSet fs) throws ConversionException {
		T copy = getBean();
		Properties properties = fs.getProperties();
		try{
			properties = getBeanProperties(copy, properties);
			for(String name : propertyNamesCache.get(getBean().getClass())){
				String value = properties.getProperty(name);
				try{
					if(org.apache.commons.lang.StringUtils.isEmpty(value)){
						continue;
					}
					org.apache.commons.beanutils.BeanUtils.setProperty(copy, name, value);
				}catch(ConversionException e){
					if(strict){
						throw e;
					}
					if(copy instanceof WarningMessageable){
						((WarningMessageable)copy).warningMessages().add(name, e);
					}
					if(copy instanceof ErrorMessageable){
						((ErrorMessageable)copy).errorMessages().add(name, e);
					}
				}
			}
		}catch(ConversionException e){
			throw e;
		}catch(Exception e){
			throw new ConversionException(e);
		}
		return copy;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org
	 * .springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	/**
	 * The bean name (id) for an object that can be populated from the field set
	 * that will be passed into {@link #mapFieldSet(FieldSet)}. Typically a
	 * prototype scoped bean so that a new instance is returned for each field
	 * set mapped.
	 * 
	 * Either this property or the type property must be specified, but not
	 * both.
	 * 
	 * @param name the name of a prototype bean in the enclosing BeanFactory
	 */
	public void setPrototypeBeanName(String name) {
		this.name = name;
	}
	
	/**
	 * Public setter for the type of bean to create instead of using a prototype
	 * bean. An object of this type will be created from its default constructor
	 * for every call to {@link #mapFieldSet(FieldSet)}.<br/>
	 * 
	 * Either this property or the prototype bean name must be specified, but
	 * not both.
	 * 
	 * @param type the type to set
	 */
	public void setTargetType(Class<? extends T> type) {
		this.type = type;
	}

	/**
	 * Check that precisely one of type or prototype bean name is specified.
	 * 
	 * @throws IllegalStateException if neither is set or both properties are
	 * set.
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.state(name != null || type != null, "Either name or type must be provided.");
		Assert.state(name == null || type == null, "Both name and type cannot be specified together.");
	}
	/**
	 * Public setter for the 'strict' property. If true, then
	 * {@link #mapFieldSet(FieldSet)} will fail of the FieldSet contains fields
	 * that cannot be mapped to the bean.
	 * 
	 * @param strict
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
	@SuppressWarnings("unchecked")
	private T getBean() {
		if (name != null) {
			return (T) beanFactory.getBean(name);
		}
		try {
			return type.newInstance();
		}
		catch (InstantiationException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		catch (IllegalAccessException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		// should not happen
		throw new IllegalStateException("Internal error: could not create bean instance for mapping.");
	}
	
	@SuppressWarnings("unchecked")
	protected Properties getBeanProperties(Object bean, Properties properties)throws IntrospectionException {

		Class<?> cls = bean.getClass();
		
		String[] namesCache = propertyNamesCache.get(cls);
		if(namesCache==null){
			List<String> setterNames = new ArrayList<String>();
			BeanInfo beanInfo = Introspector.getBeanInfo(cls);
			PropertyDescriptor propDescs[] = beanInfo.getPropertyDescriptors();
			for(PropertyDescriptor propDesc: propDescs){
				if(propDesc.getWriteMethod()!=null){
					setterNames.add(propDesc.getName());
				}
			}
			propertyNamesCache.put(cls, setterNames.toArray(new String[0]));
		}
		// Map from field names to property names
		Map<String, String> matches = propertiesMatched.get(cls);
		if (matches == null) {
			matches = new HashMap<String, String>();
			propertiesMatched.put(cls, matches);
		}
		
		Set<String> keys = new HashSet(properties.keySet());
		for (String key : keys) {

			if (matches.containsKey(key)) {
				switchPropertyNames(properties, key, matches.get(key));
				continue;
			}

			String name = findPropertyName(bean, key);

			if (name != null) {
				matches.put(key, name);
				switchPropertyNames(properties, key, name);
			}
		}

		return properties;
	}

	private String findPropertyName(Object bean, String key) {

		if (bean == null) {
			return null;
		}

		Class<?> cls = bean.getClass();

		int index = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(key);
		String prefix;
		String suffix;

		// If the property name is nested recurse down through the properties
		// looking for a match.
		if (index > 0) {
			prefix = key.substring(0, index);
			suffix = key.substring(index + 1, key.length());
			String nestedName = findPropertyName(bean, prefix);
			if (nestedName == null) {
				return null;
			}

			Object nestedValue = getPropertyValue(bean, nestedName);
			String nestedPropertyName = findPropertyName(nestedValue, suffix);
			return nestedPropertyName == null ? null : nestedName + "." + nestedPropertyName;
		}

		String name = null;
		int distance = 0;
		index = key.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);

		if (index > 0) {
			prefix = key.substring(0, index);
			suffix = key.substring(index);
		}
		else {
			prefix = key;
			suffix = "";
		}

		while (name == null && distance <= distanceLimit) {
			String[] candidates = PropertyMatches.forProperty(prefix, cls, distance).getPossibleMatches();
			// If we find precisely one match, then use that one...
			if (candidates.length == 1) {
				String candidate = candidates[0];
				if (candidate.equals(prefix)) { // if it's the same don't
					// replace it...
					name = key;
				}
				else {
					name = candidate + suffix;
				}
			}
			distance++;
		}
		return name;
	}

	private Object getPropertyValue(Object bean, String nestedName) {
		BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
		Object nestedValue = wrapper.getPropertyValue(nestedName);
		if (nestedValue == null) {
			try {
				nestedValue = wrapper.getPropertyType(nestedName).newInstance();
				wrapper.setPropertyValue(nestedName, nestedValue);
			}
			catch (InstantiationException e) {
				ReflectionUtils.handleReflectionException(e);
			}
			catch (IllegalAccessException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
		return nestedValue;
	}

	private void switchPropertyNames(Properties properties, String oldName, String newName) {
		String value = properties.getProperty(oldName);
		properties.remove(oldName);
		properties.setProperty(newName, value);
	}
	
	
	/**
	 * Helper class for calculating bean property matches, according to.
	 * Used by BeanWrapperImpl to suggest alternatives for an invalid property name.<br/>
	 * 
	 * Copied and slightly modified from Spring core,
	 *
	 * @author Alef Arendsen
	 * @author Arjen Poutsma
	 * @author Juergen Hoeller
	 * @author Dave Syer
	 * 
	 * @since 1.0
	 * @see #forProperty(String, Class)
	 */
	final static class PropertyMatches {
		
		//---------------------------------------------------------------------
		// Static section
		//---------------------------------------------------------------------

		/** Default maximum property distance: 2 */
		public static final int DEFAULT_MAX_DISTANCE = 2;


		/**
		 * Create PropertyMatches for the given bean property.
		 * @param propertyName the name of the property to find possible matches for
		 * @param beanClass the bean class to search for matches
		 */
		public static PropertyMatches forProperty(String propertyName, Class<?> beanClass) {
			return forProperty(propertyName, beanClass, DEFAULT_MAX_DISTANCE);
		}

		/**
		 * Create PropertyMatches for the given bean property.
		 * @param propertyName the name of the property to find possible matches for
		 * @param beanClass the bean class to search for matches
		 * @param maxDistance the maximum property distance allowed for matches
		 */
		public static PropertyMatches forProperty(String propertyName, Class<?> beanClass, int maxDistance) {
			return new PropertyMatches(propertyName, beanClass, maxDistance);
		}


		//---------------------------------------------------------------------
		// Instance section
		//---------------------------------------------------------------------

		private final String propertyName;

		private String[] possibleMatches;


		/**
		 * Create a new PropertyMatches instance for the given property.
		 */
		private PropertyMatches(String propertyName, Class<?> beanClass, int maxDistance) {
			this.propertyName = propertyName;
			this.possibleMatches = calculateMatches(BeanUtils.getPropertyDescriptors(beanClass), maxDistance);
		}


		/**
		 * Return the calculated possible matches.
		 */
		public String[] getPossibleMatches() {
			return possibleMatches;
		}

		/**
		 * Build an error message for the given invalid property name,
		 * indicating the possible property matches.
		 */
		public String buildErrorMessage() {
			StringBuffer buf = new StringBuffer();
			buf.append("Bean property '");
			buf.append(this.propertyName);
			buf.append("' is not writable or has an invalid setter method. ");

			if (ObjectUtils.isEmpty(this.possibleMatches)) {
				buf.append("Does the parameter type of the setter match the return type of the getter?");
			}
			else {
				buf.append("Did you mean ");
				for (int i = 0; i < this.possibleMatches.length; i++) {
					buf.append('\'');
					buf.append(this.possibleMatches[i]);
					if (i < this.possibleMatches.length - 2) {
						buf.append("', ");
					}
					else if (i == this.possibleMatches.length - 2){
						buf.append("', or ");
					}
		 		}
				buf.append("'?");
			}
			return buf.toString();
		}


		/**
		 * Generate possible property alternatives for the given property and
		 * class. Internally uses the <code>getStringDistance</code> method, which
		 * in turn uses the Levenshtein algorithm to determine the distance between
		 * two Strings.
		 * @param propertyDescriptors the JavaBeans property descriptors to search
		 * @param maxDistance the maximum distance to accept
		 */
		private String[] calculateMatches(PropertyDescriptor[] propertyDescriptors, int maxDistance) {
			List<String> candidates = new ArrayList<String>();
			for (int i = 0; i < propertyDescriptors.length; i++) {
				if (propertyDescriptors[i].getWriteMethod() != null) {
					String possibleAlternative = propertyDescriptors[i].getName();
					if (calculateStringDistance(this.propertyName, possibleAlternative) <= maxDistance) {
						candidates.add(possibleAlternative);
					}
				}
			}
			Collections.sort(candidates);
			return StringUtils.toStringArray(candidates);
		}

		/**
		 * Calculate the distance between the given two Strings
		 * according to the Levenshtein algorithm.
		 * @param s1 the first String
		 * @param s2 the second String
		 * @return the distance value
		 */
		private int calculateStringDistance(String s1, String s2) {
			if (s1.length() == 0) {
				return s2.length();
			}
			if (s2.length() == 0) {
				return s1.length();
			}
			int d[][] = new int[s1.length() + 1][s2.length() + 1];

			for (int i = 0; i <= s1.length(); i++) {
				d[i][0] = i;
			}
			for (int j = 0; j <= s2.length(); j++) {
				d[0][j] = j;
			}

			for (int i = 1; i <= s1.length(); i++) {
				char s_i = s1.charAt(i - 1);
				for (int j = 1; j <= s2.length(); j++) {
					int cost;
					char t_j = s2.charAt(j - 1);
					if (Character.toLowerCase(s_i) == Character.toLowerCase(t_j)) {
						cost = 0;
					} else {
						cost = 1;
					}
					d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1),
							d[i - 1][j - 1] + cost);
				}
			}

			return d[s1.length()][s2.length()];
		}

	}
}
