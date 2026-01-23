/**
 * Spring Data JPA integration for JSON query support.
 *
 * <p>
 * This package provides integration with Spring Data JPA's QueryDSL repository
 * support, offering a convenient base class for repositories that need to use
 * MySQL JSON functions.
 * </p>
 *
 * <h2>Main Classes</h2>
 * <ul>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.jpa.spring.JsonFunctionRepositorySupport}
 * - Base repository support class with JSON function access</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * Extend {@code JsonFunctionRepositorySupport} in your custom repository
 * implementations to gain access to pre-configured JSON functions for use in
 * QueryDSL queries.
 * </p>
 *
 * @author snowykte0426
 * @since 0.1.0
 */
package io.github.snowykte0426.querydsl.mysql.json.jpa.spring;
