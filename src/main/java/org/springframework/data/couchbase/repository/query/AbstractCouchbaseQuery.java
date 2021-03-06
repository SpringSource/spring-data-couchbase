/*
 * Copyright 2020 the original author or authors
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
package org.springframework.data.couchbase.repository.query;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.couchbase.core.CouchbaseOperations;
import org.springframework.data.couchbase.core.ExecutableFindByQueryOperation;
import org.springframework.data.couchbase.core.ExecutableFindByQueryOperation.ExecutableFindByQuery;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.query.CouchbaseQueryExecution.DeleteExecution;
import org.springframework.data.couchbase.repository.query.CouchbaseQueryExecution.PagedExecution;
import org.springframework.data.repository.core.EntityMetadata;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link RepositoryQuery} implementation for Couchbase.
 *
 * @author Michael Reiche
 * @since 4.1
 */
public abstract class AbstractCouchbaseQuery extends AbstractCouchbaseQueryBase<CouchbaseOperations>
		implements RepositoryQuery {

	private final ExecutableFindByQueryOperation.ExecutableFindByQuery<?> findOperationWithProjection;

	/**
	 * Creates a new {@link AbstractCouchbaseQuery} from the given {@link ReactiveCouchbaseQueryMethod} and
	 * {@link org.springframework.data.couchbase.core.CouchbaseOperations}.
	 *
	 * @param method must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 * @param expressionParser must not be {@literal null}.
	 * @param evaluationContextProvider must not be {@literal null}.
	 */
	public AbstractCouchbaseQuery(CouchbaseQueryMethod method, CouchbaseOperations operations,
			SpelExpressionParser expressionParser, QueryMethodEvaluationContextProvider evaluationContextProvider) {
		super(method, operations, expressionParser, evaluationContextProvider);
		Assert.notNull(method, "CouchbaseQueryMethod must not be null!");
		Assert.notNull(operations, "ReactiveCouchbaseOperations must not be null!");
		Assert.notNull(expressionParser, "SpelExpressionParser must not be null!");
		Assert.notNull(evaluationContextProvider, "QueryMethodEvaluationContextProvider must not be null!");
		// this.operations = operations;
		EntityMetadata<?> metadata = method.getEntityInformation();
		Class<?> type = metadata.getJavaType();
		this.findOperationWithProjection = operations.findByQuery(type);
	}

	/**
	 * Execute the {@link RepositoryQuery} of the given method with the parameters provided by the
	 * {@link ParametersParameterAccessor accessor}
	 *
	 * @param method the {@link ReactiveCouchbaseQueryMethod} invoked. Never {@literal null}.
	 * @param processor {@link ResultProcessor} for post procession. Never {@literal null}.
	 * @param accessor for providing invocation arguments. Never {@literal null}.
	 * @param typeToRead the desired component target type. Can be {@literal null}.
	 */
	@Override
	protected Object doExecute(CouchbaseQueryMethod method, ResultProcessor processor,
			ParametersParameterAccessor accessor, @Nullable Class<?> typeToRead) {

		Query query = createQuery(accessor);

		query = applyAnnotatedConsistencyIfPresent(query);
		// query = applyAnnotatedCollationIfPresent(query, accessor); // not yet implemented

		ExecutableFindByQuery<?> find = typeToRead == null ? findOperationWithProjection //
				: findOperationWithProjection; // not yet implemented in core .as(typeToRead);

		String collection = "_default._default";// method.getEntityInformation().getCollectionName(); // not yet implemented

		CouchbaseQueryExecution execution = getExecution(accessor,
				new ResultProcessingConverter<>(processor, getOperations(), getInstantiators()), find);
		return execution.execute(query, processor.getReturnedType().getDomainType(), collection);
	}

	/**
	 * Returns the execution instance to use.
	 *
	 * @param accessor must not be {@literal null}.
	 * @param resultProcessing must not be {@literal null}.
	 * @return
	 */
	private CouchbaseQueryExecution getExecution(ParameterAccessor accessor, Converter<Object, Object> resultProcessing,
			ExecutableFindByQueryOperation.ExecutableFindByQuery<?> operation) {
		return new CouchbaseQueryExecution.ResultProcessingExecution(getExecutionToWrap(accessor, operation),
				resultProcessing);
	}

	/**
	 * Returns the execution to wrap
	 *
	 * @param accessor must not be {@literal null}.
	 * @param operation must not be {@literal null}.
	 * @return
	 */
	private CouchbaseQueryExecution getExecutionToWrap(ParameterAccessor accessor,
			ExecutableFindByQueryOperation.ExecutableFindByQuery<?> operation) {

		if (isDeleteQuery()) {
			return new DeleteExecution(getOperations(), getQueryMethod());
		} else if (isTailable(getQueryMethod())) {
			return (q, t, c) -> operation.matching(q.with(accessor.getPageable())).all(); // s/b tail() instead of all()
		} else if (getQueryMethod().isCollectionQuery()) {
			return (q, t, c) -> operation.matching(q.with(accessor.getPageable())).all();
		} else if (isCountQuery()) {
			return (q, t, c) -> operation.matching(q).count();
		} else if (isExistsQuery()) {
			return (q, t, c) -> operation.matching(q).exists();
		} else if (getQueryMethod().isPageQuery()) {
			return new PagedExecution(operation, accessor.getPageable());
		} else {
			return (q, t, c) -> {
				ExecutableFindByQueryOperation.TerminatingFindByQuery<?> find = operation.matching(q);
				if (isCountQuery()) {
					return find.count();
				}
				return isLimiting() ? find.first() : find.one();
			};
		}
	}

	/**
	 * Apply Meta annotation to query
	 *
	 * @param query must not be {@literal null}.
	 * @return Query
	 */
	Query applyQueryMetaAttributesWhenPresent(Query query) {

		if (getQueryMethod().hasQueryMetaAttributes()) {
			query.setMeta(getQueryMethod().getQueryMetaAttributes());
		}

		return query;
	}
}
