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

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.isEmpty;
import io.crate.action.sql.SQLBulkResponse.Result;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Hasnain Javed
 * @since 1.0.0
 * @param <T>
 */
public class BulkActionResult<T> implements ActionableResult<T> {
	
	private List<ActionResult<T>> results;
	
	public BulkActionResult() {
		results = new LinkedList<ActionResult<T>>();
	}
	
	public ActionResult<T> addResult(Result result, T source) {
		ActionResult<T> actionResult = new ActionResult<T>(result, source); 
		results.add(actionResult);
		return actionResult;
	}
	
	@Override
	public List<ActionResult<T>> getResults() {
		return results;
	}
	
	@Override
	public List<ActionResult<T>> getFailures() {
		
		List<ActionResult<T>> failures = new LinkedList<ActionResult<T>>();
		
		for(ActionResult<T> result : results) {
			if(result.isFailure()) {
				failures.add(result);
			}
		}
		
		return failures;
	}
	
	@Override
	public List<ActionResult<T>> getSuccesses() {
		
		List<ActionResult<T>> successes = new LinkedList<ActionResult<T>>();
		
		for(ActionResult<T> result : results) {
			if(result.isSuccess()) {
				successes.add(result);
			}
		}
		
		return successes;
	}

	/**
	 * 
	 * @author Hasnain Javed
	 * @since 1.0.0
	 * @param <T>
	 */
	public static class ActionResult<T> {
		
		private Result result;
		private T source;
		
		public ActionResult(Result result, T source) {
			
			notNull(result);
			notNull(source);
			
			this.result = result;
			this.source = source;
		}

		public Result getResult() {
			return result;
		}

		public T getSource() {
			return source;
		}
		
		public boolean isFailure() {
			return !isSuccess();
		}
		
		public boolean isSuccess() {
			return (result.rowCount() == 1 && isEmpty(result.errorMessage()));
		}
	}
}