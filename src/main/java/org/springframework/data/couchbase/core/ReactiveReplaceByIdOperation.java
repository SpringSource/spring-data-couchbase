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
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;

import org.springframework.data.couchbase.core.support.InCollection;
import org.springframework.data.couchbase.core.support.InScope;
import org.springframework.data.couchbase.core.support.OneAndAllEntityReactive;
import org.springframework.data.couchbase.core.support.WithReplaceOptions;

import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.kv.PersistTo;
import com.couchbase.client.java.kv.ReplaceOptions;
import com.couchbase.client.java.kv.ReplicateTo;

public interface ReactiveReplaceByIdOperation {

	<T> ReactiveReplaceById<T> replaceById(Class<T> domainType);

	interface TerminatingReplaceById<T> extends OneAndAllEntityReactive<T> {

		Mono<T> one(T object);

		Flux<? extends T> all(Collection<? extends T> objects);

	}

	interface ReplaceByIdWithOptions<T> extends TerminatingReplaceById<T>, WithReplaceOptions<RemoveResult> {
		TerminatingReplaceById<T> withOptions(ReplaceOptions options);
	}

	interface ReplaceByIdInCollection<T> extends ReplaceByIdWithOptions<T>, InCollection<Object> {
		ReplaceByIdWithOptions<T> inCollection(String collection);
	}

	interface ReplaceByIdInScope<T> extends ReplaceByIdInCollection<T>, InScope<Object> {
		ReplaceByIdInCollection<T> inScope(String scope);
	}

	interface ReplaceByIdWithDurability<T> extends ReplaceByIdInScope<T>, WithDurability<T> {

		ReplaceByIdInCollection<T> withDurability(DurabilityLevel durabilityLevel);

		ReplaceByIdInCollection<T> withDurability(PersistTo persistTo, ReplicateTo replicateTo);

	}

	interface ReplaceByIdWithExpiry<T> extends ReplaceByIdWithDurability<T>, WithExpiry<T> {

		ReplaceByIdWithDurability<T> withExpiry(final Duration expiry);
	}

	interface ReactiveReplaceById<T> extends ReplaceByIdWithExpiry<T> {}

}
