package org.springframework.data.crate.core;

import static org.slf4j.LoggerFactory.getLogger;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class CreateTemplate implements CrateOperations {
	
	private final Logger logger;
	private final NamedParameterJdbcOperations jdbcTemplate;
	
	public CreateTemplate(DataSource dataSource) {
		super();
		logger = getLogger(getClass());
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}
}