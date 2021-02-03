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

import java.time.Duration;
import java.util.Collection;

import org.springframework.data.couchbase.core.support.InCollection;
import org.springframework.data.couchbase.core.support.InScope;
import org.springframework.data.couchbase.core.support.OneAndAllEntity;
import org.springframework.data.couchbase.core.support.WithUpsertOptions;

import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.kv.PersistTo;
import com.couchbase.client.java.kv.ReplicateTo;
import com.couchbase.client.java.kv.UpsertOptions;

public interface ExecutableUpsertByIdOperation {

	<T> ExecutableUpsertById<T> upsertById(Class<T> domainType);

	interface TerminatingUpsertById<T> extends OneAndAllEntity<T> {

		@Override
		T one(T object);

		@Override
		Collection<? extends T> all(Collection<? extends T> objects);

	}

	interface UpsertByIdWithOptions<T> extends TerminatingUpsertById<T>, WithUpsertOptions<T> {
		TerminatingUpsertById<T> withOptions(UpsertOptions options);
	}

	interface UpsertByIdInCollection<T> extends UpsertByIdWithOptions<T>, InCollection<T> {
		UpsertByIdWithOptions<T> inCollection(String collection);
	}

	interface UpsertByIdInScope<T> extends UpsertByIdInCollection<T>, InScope<T> {
		UpsertByIdInCollection<T> inScope(String scope);
	}

	interface UpsertByIdWithDurability<T> extends UpsertByIdInScope<T>, WithDurability<T> {

		UpsertByIdInScope<T> withDurability(DurabilityLevel durabilityLevel);

		UpsertByIdInScope<T> withDurability(PersistTo persistTo, ReplicateTo replicateTo);

	}

	interface UpsertByIdWithExpiry<T> extends UpsertByIdWithDurability<T>, WithExpiry<T> {

		UpsertByIdWithDurability<T> withExpiry(Duration expiry);
	}

	interface ExecutableUpsertById<T> extends UpsertByIdWithExpiry<T> {}

}
