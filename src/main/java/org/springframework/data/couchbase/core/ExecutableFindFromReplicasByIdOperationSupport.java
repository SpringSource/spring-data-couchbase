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

import org.springframework.data.couchbase.core.ReactiveFindFromReplicasByIdOperationSupport.ReactiveFindFromReplicasByIdSupport;

import com.couchbase.client.java.kv.GetAnyReplicaOptions;

public class ExecutableFindFromReplicasByIdOperationSupport implements ExecutableFindFromReplicasByIdOperation {

	private final CouchbaseTemplate template;

	ExecutableFindFromReplicasByIdOperationSupport(CouchbaseTemplate template) {
		this.template = template;
	}

	@Override
	public <T> ExecutableFindFromReplicasById<T> findFromReplicasById(Class<T> domainType) {
		return new ExecutableFindFromReplicasByIdSupport<>(template, domainType, domainType, null, null, null);
	}

	static class ExecutableFindFromReplicasByIdSupport<T> implements ExecutableFindFromReplicasById<T> {

		private final CouchbaseTemplate template;
		private final Class<?> domainType;
		private final Class<T> returnType;
		private final String scope;
		private final String collection;
		private final GetAnyReplicaOptions options;
		private final ReactiveFindFromReplicasByIdSupport<T> reactiveSupport;

		ExecutableFindFromReplicasByIdSupport(CouchbaseTemplate template, Class<?> domainType, Class<T> returnType,
				String scope, String collection, GetAnyReplicaOptions options) {
			this.template = template;
			this.domainType = domainType;
			this.scope = scope;
			this.collection = collection;
			this.options = options;
			this.returnType = returnType;
			this.reactiveSupport = new ReactiveFindFromReplicasByIdSupport<>(template.reactive(), domainType, returnType,
					scope, collection, options);
		}

		@Override
		public T any(String id) {
			return reactiveSupport.any(id).block();
		}

		@Override
		public Collection<? extends T> any(Collection<String> ids) {
			return reactiveSupport.any(ids).collectList().block();
		}

		@Override
		public TerminatingFindFromReplicasById<T> withOptions(GetAnyReplicaOptions options) {
			return new ExecutableFindFromReplicasByIdSupport<>(template, domainType, returnType, scope, collection, options);
		}

		@Override
		public FindFromReplicasByIdWithOptions<T> inCollection(final String collection) {
			return new ExecutableFindFromReplicasByIdSupport<>(template, domainType, returnType, scope, collection, options);
		}

		@Override
		public FindFromReplicasByIdInCollection<T> inScope(String scope) {
			return new ExecutableFindFromReplicasByIdSupport<>(template, domainType, returnType, scope, collection, options);
		}

	}

}
