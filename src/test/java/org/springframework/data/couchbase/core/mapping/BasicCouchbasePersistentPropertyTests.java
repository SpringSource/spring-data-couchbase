/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.couchbase.core.mapping;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the correct behavior of properties on persistable objects.
 *
 * @author Michael Nitschinger
 * @author Mark Paluch
 */
public class BasicCouchbasePersistentPropertyTests {

  /**
   * Holds the entity to test against (contains the properties).
   */
  CouchbasePersistentEntity<Beer> entity;

  /**
   * Create an instance of the demo entity.
   */
  @Before
  public void setUp() {
    entity = new BasicCouchbasePersistentEntity<Beer>(
            ClassTypeInformation.from(Beer.class));
  }

  /**
   * Verifies the name of the property without annotations.
   */
  @Test
  public void usesPropertyFieldName() {
    Field field = ReflectionUtils.findField(Beer.class, "description");
    assertThat(getPropertyFor(field).getFieldName()).isEqualTo("description");
  }

  /**
   * Verifies the name of the property with custom name annotation.
   */
  @Test
  public void usesAnnotatedFieldName() {
    Field field = ReflectionUtils.findField(Beer.class, "name");
    assertThat(getPropertyFor(field).getFieldName()).isEqualTo("foobar");
  }

  @Test
  public void testPrefersSpringIdAnnotation() {
    BasicCouchbasePersistentEntity<Beer> test = new BasicCouchbasePersistentEntity<Beer>(
            ClassTypeInformation.from(Beer.class));

    Field sdkIdField = ReflectionUtils.findField(Beer.class, "sdkId");
    CouchbasePersistentProperty sdkIdProperty = getPropertyFor(sdkIdField);
    Field springIdField = ReflectionUtils.findField(Beer.class, "springId");
    CouchbasePersistentProperty springIdProperty = getPropertyFor(springIdField);
    test.addPersistentProperty(sdkIdProperty);
    test.addPersistentProperty(springIdProperty);

    assertThat(sdkIdProperty.getFieldName()).isEqualTo("sdkId");
    assertThat(springIdProperty.getFieldName()).isEqualTo("springId");

    assertThat(sdkIdProperty.isIdProperty()).isTrue();
    assertThat(springIdProperty.isIdProperty()).isTrue();

    CouchbasePersistentProperty property = test.getIdProperty();
    assertThat(property).isEqualTo(springIdProperty);
  }

  @Test
  public void testAcceptsSdkIdAnnotation() {
    BasicCouchbasePersistentEntity<SdkIdentified> test = new BasicCouchbasePersistentEntity<SdkIdentified>(
            ClassTypeInformation.from(SdkIdentified.class));
    Field id = ReflectionUtils.findField(SdkIdentified.class, "id");
    CouchbasePersistentProperty idProperty = getPropertyFor(id);
    test.addPersistentProperty(idProperty);

    CouchbasePersistentProperty property = test.getIdProperty();
    assertThat(property).isEqualTo(idProperty);
  }

  @Test
  public void testSdkIdAnnotationEvaluatedAfterSpringIdAnnotationIsIgnored() {
    BasicCouchbasePersistentEntity<Beer> test = new BasicCouchbasePersistentEntity<Beer>(
            ClassTypeInformation.from(Beer.class));
    Field sdkIdField = ReflectionUtils.findField(Beer.class, "sdkId");
    CouchbasePersistentProperty sdkIdProperty = getPropertyFor(sdkIdField);
    Field springIdField = ReflectionUtils.findField(Beer.class, "springId");
    CouchbasePersistentProperty springIdProperty = getPropertyFor(springIdField);

    //here this simulates the order in which the annotations would be found
    // when "overriding" Spring @Id with SDK's @Id...
    test.addPersistentProperty(springIdProperty);

    assertThat(test.getIdProperty()).isEqualTo(springIdProperty);

    test.addPersistentProperty(sdkIdProperty);
    assertThat(test.getIdProperty()).isEqualTo(springIdProperty);
  }

  /**
   * Helper method to create a property out of the field.
   *
   * @param field the field to retrieve the properties from.
   * @return the actual BasicCouchbasePersistentProperty instance.
   */
  private CouchbasePersistentProperty getPropertyFor(Field field) {

		ClassTypeInformation<?> type = ClassTypeInformation.from(field.getDeclaringClass());

		return new BasicCouchbasePersistentProperty(Property.of(type, field), entity, SimpleTypeHolder.DEFAULT,
				PropertyNameFieldNamingStrategy.INSTANCE);
  }

  /**
   * Simple POJO to test attribute properties and annotations.
   */
  public class Beer {

    @com.couchbase.client.java.repository.annotation.Id
    private String sdkId;

    @Id
    private String springId;

    @com.couchbase.client.java.repository.annotation.Field("foobar")
    String name;

    String description;

    public String getId() {
      return springId;
    }
  }

  /**
   * Simple POJO to test that a single ID property from the SDK is taken into account.
   */
  public class SdkIdentified {
    @com.couchbase.client.java.repository.annotation.Id
    private String id;

    String value;
  }
}
