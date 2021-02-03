/*
 * Copyright 2012-2021 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.couchbase.core;

import reactor.core.publisher.Flux;

import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.core.query.QueryCriteriaDefinition;
import org.springframework.data.couchbase.core.support.InCollection;
import org.springframework.data.couchbase.core.support.InScope;
import org.springframework.data.couchbase.core.support.WithConsistency;
import org.springframework.data.couchbase.core.support.WithQuery;
import org.springframework.data.couchbase.core.support.WithQueryOptions;

import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryScanConsistency;

public interface ReactiveRemoveByQueryOperation {

	<T> ReactiveRemoveByQuery<T> removeByQuery(Class<T> domainType);

	interface TerminatingRemoveByQuery<T> {
		Flux<RemoveResult> all();
	}

	interface RemoveByQueryWithQuery<T> extends TerminatingRemoveByQuery<T>, WithQuery<RemoveResult> {

		TerminatingRemoveByQuery<T> matching(Query query);

		default TerminatingRemoveByQuery<T> matching(QueryCriteriaDefinition criteria) {
			return matching(Query.query(criteria));
		}
	}

	interface RemoveByQueryWithOptions<T> extends RemoveByQueryWithQuery<T>, WithQueryOptions<RemoveResult> {
		RemoveByQueryWithQuery<T> withOptions(QueryOptions options);
	}

	interface RemoveByQueryInCollection<T> extends RemoveByQueryWithOptions<T>, InCollection<Object> {
		RemoveByQueryWithOptions<T> inCollection(String collection);
	}

	interface RemoveByQueryInScope<T> extends RemoveByQueryInCollection<T>, InScope<Object> {
		RemoveByQueryInCollection<T> inScope(String scope);
	}

	@Deprecated
	interface RemoveByQueryConsistentWith<T> extends RemoveByQueryInScope<T> {

		@Deprecated
		RemoveByQueryInScope<T> consistentWith(QueryScanConsistency scanConsistency);

	}

	interface RemoveByQueryWithConsistency<T> extends RemoveByQueryConsistentWith<T>, WithConsistency<RemoveResult> {

		RemoveByQueryConsistentWith<T> withConsistency(QueryScanConsistency scanConsistency);

	}

	interface ReactiveRemoveByQuery<T> extends RemoveByQueryWithConsistency<T> {}

}
