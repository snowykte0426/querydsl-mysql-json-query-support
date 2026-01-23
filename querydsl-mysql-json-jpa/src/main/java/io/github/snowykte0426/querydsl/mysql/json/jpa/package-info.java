/**
 * JPA/Hibernate integration for MySQL JSON query support.
 *
 * <p>
 * This package provides integration between the core JSON query functionality
 * and JPA/Hibernate, enabling the use of MySQL JSON functions in JPA queries
 * through QueryDSL.
 * </p>
 *
 * <h2>Main Classes</h2>
 * <ul>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.jpa.JPAJsonFunctions} - JPA-specific JSON function wrappers</li>
 * </ul>
 *
 * <h2>Key Sub-packages</h2>
 * <ul>
 * <li>{@code expressions} - JPA-specific JSON expression types</li>
 * <li>{@code hibernate} - Hibernate function contributor for automatic registration</li>
 * <li>{@code spring} - Spring Data JPA repository support</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0
 */
package io.github.snowykte0426.querydsl.mysql.json.jpa;
