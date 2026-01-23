/**
 * Hibernate function contributor for automatic JSON function registration.
 *
 * <p>
 * This package provides Hibernate SPI integration that automatically registers
 * MySQL JSON functions with Hibernate's function registry, making them available
 * in JPQL and Criteria queries without manual configuration.
 * </p>
 *
 * <h2>Main Classes</h2>
 * <ul>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.jpa.hibernate.MySQLJsonFunctionContributor} - Hibernate FunctionContributor implementation for JSON functions</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * The function contributor is automatically discovered and registered by Hibernate
 * through Java's ServiceLoader mechanism. No manual configuration is required.
 * </p>
 *
 * @author snowykte0426
 * @since 0.1.0
 */
package io.github.snowykte0426.querydsl.mysql.json.jpa.hibernate;
