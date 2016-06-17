/*
 *
 *  * Copyright (c) 2002-2015 "Neo Technology,"
 *  * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *  *
 *  * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *  * You may not use this product except in compliance with the License.
 *  *
 *  * This product may include a number of subcomponents with
 *  * separate copyright notices and license terms. Your use of the source
 *  * code for these subcomponents is subject to the terms and
 *  * conditions of the subcomponent's license, as noted in the LICENSE file.
 *  *
 *
 */

package converters;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Luanne Misquitta
 */
public class Issue102 {
	public static void main(String[] args)
	{
		Configuration configuration = new Configuration();
		new File("target/neo4j").mkdirs();
		String uri = "file:" + new File("target/neo4j").getAbsolutePath();
		configuration.driverConfiguration()
				.setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver").setURI(uri);
		SessionFactory sessionFactory = new SessionFactory(configuration, "converters");

		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		Map<String, String> parameters = new HashMap<>();
		parameters.put("param1", "value1");
		JdbcDriver driver = new JdbcDriver("MyDriver", parameters);
		session.save(driver);
		transaction.commit();

		transaction = session.beginTransaction();
		session.load(JdbcDriver.class, driver.getId());
		transaction.commit();
	}

	/**
	 */
	@NodeEntity
	public static class JdbcDriver
	{
		@GraphId
		private Long id;
		@Property
		private String name;
		@Convert(MyStringMapConverter.class)
		private Map<String, String> parameters;

		public JdbcDriver()
		{}

		public JdbcDriver(String name, Map<String, String> parameters)
		{
			this.name = name;
			this.parameters = parameters;
		}

		public Long getId()
		{
			return id;
		}

		public void setId(Long id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public Map<String, String> getParameters()
		{
			return parameters;
		}

		public void setParameters(Map<String, String> parameters)
		{
			this.parameters = parameters;
		}
	}

	/**
	 */
	public static class MyStringMapConverter implements AttributeConverter<Map<String, String>, List<String>>
	{
		private static final String DELIMITER = "|";

		@Override
		public List<String> toGraphProperty(Map<String, String> value)
		{
			if (value != null)
			{
				return value.entrySet().stream().map(entry -> entry.getKey() + DELIMITER + entry.getValue()).collect(Collectors.toList());
			}
			else
			{
				return null;
			}
		}

		@Override
		public Map<String, String> toEntityAttribute(List<String> value)
		{
			if (value != null)
			{
				return value.stream().collect(Collectors.toMap(v -> v.substring(0, v.indexOf(DELIMITER)), v -> v.substring(v.indexOf(DELIMITER) + 1, v.length())));
			}
			else
			{
				return null;
			}
		}
	}
}
