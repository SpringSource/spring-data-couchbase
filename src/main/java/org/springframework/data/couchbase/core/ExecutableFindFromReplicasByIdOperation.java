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

import java.util.Collection;

import org.springframework.data.couchbase.core.support.AnyId;
import org.springframework.data.couchbase.core.support.InCollection;
import org.springframework.data.couchbase.core.support.InScope;
import org.springframework.data.couchbase.core.support.WithGetAnyReplicaOptions;

import com.couchbase.client.java.kv.GetAnyReplicaOptions;

public interface ExecutableFindFromReplicasByIdOperation {

	<T> ExecutableFindFromReplicasById<T> findFromReplicasById(Class<T> domainType);

	interface TerminatingFindFromReplicasById<T> extends AnyId<T> {

		T any(String id);

		Collection<? extends T> any(Collection<String> ids);

	}

	interface FindFromReplicasByIdWithOptions<T> extends TerminatingFindFromReplicasById<T>, WithGetAnyReplicaOptions<T> {
		TerminatingFindFromReplicasById<T> withOptions(GetAnyReplicaOptions options);
	}

	interface FindFromReplicasByIdInCollection<T> extends FindFromReplicasByIdWithOptions<T>, InCollection<T> {
		FindFromReplicasByIdWithOptions<T> inCollection(String collection);
	}

	interface FindFromReplicasByIdInScope<T> extends FindFromReplicasByIdInCollection<T>, InScope<T> {
		FindFromReplicasByIdInCollection<T> inScope(String scope);
	}

	interface ExecutableFindFromReplicasById<T> extends FindFromReplicasByIdInScope<T> {}

}
