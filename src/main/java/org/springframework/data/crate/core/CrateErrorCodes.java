/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.crate.core;

/**
 * @author Hasnain Javed
 * @since 1.0.0
 * Error codes copied from https://crate.io/docs/stable/sql/rest.html
 */
abstract class CrateErrorCodes {
    public static final int INVALID_SQL_STATEMENT_OR_SYNTAX = 4000;
    public static final int INVALID_ANALYZER_DEFINITION = 4001;
    public static final int INVALID_TABLE_NAME = 4002;
    public static final int FIELD_VALIDATION_FAILED = 4003;
    public static final int FEATURE_NOT_SUPPORTED_YET = 4004;
    public static final int ALTER_TABLE_ALIAS_NOT_SUPPORTED = 4005;
    public static final int COLUMN_ALIAS_AMBIGUOUS = 4006;
    public static final int UNKNOWN_TABLE = 4041;
    public static final int UNKNOWN_ANALYZER = 4042;
    public static final int UNKNOWN_COLUMN = 4043;
    public static final int UNKNOWN_TYPE = 4044;
    public static final int UNKNOWN_SCHEMA = 4045;
    public static final int UNKNOWN_PARTITION = 4046;
    public static final int DUPLICATE_PRIMARY_KEY = 4091;
    public static final int VERSION_CONFLICT = 4092;
    public static final int DUPLICATE_TABLE_NAME = 4093;
    public static final int TABLE_ALIAS_CONTAINS_TABLES_WITH_DIFFERENT_SCHEMA = 4094;
    public static final int UNHANDLED_SERVER_ERROR = 5000;
    public static final int TASKS_EXECUTION_FAILED = 5001;
    public static final int SHARDS_NOT_AVAILABLE = 5002;
    public static final int QUERY_FAILED_ON_SHARDS = 5003;
}
