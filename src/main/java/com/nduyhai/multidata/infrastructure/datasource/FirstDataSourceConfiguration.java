package com.nduyhai.multidata.infrastructure.datasource;

import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.DATASOURCE;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.DATASOURCE_PROPERTIES;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.ENTITY_MANAGER;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.ENTITY_PACKAGE;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.JPA_PROPERTIES;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.PREFIX_DATASOURCE;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.PREFIX_PROPERTIES;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.REPO_PACKAGE;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.TRANSACTION_MANAGER;
import static com.nduyhai.multidata.infrastructure.datasource.DataSourceConstant.First.UNIT;

import com.nduyhai.multidata.infrastructure.config.FlexyPoolConfiguration;
import com.vladmihalcea.flexypool.FlexyPoolDataSource;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = ENTITY_MANAGER,
    transactionManagerRef = TRANSACTION_MANAGER,
    basePackages = {REPO_PACKAGE})
public class FirstDataSourceConfiguration extends AbstractMultipleDataSource {

  @Primary
  @Bean(name = JPA_PROPERTIES)
  @ConfigurationProperties(prefix = PREFIX_PROPERTIES)
  public JpaProperties properties() {
    return new JpaProperties();
  }

  @Primary
  @Bean(DATASOURCE_PROPERTIES)
  @ConfigurationProperties(prefix = PREFIX_DATASOURCE)
  public DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Primary
  @Bean(name = DATASOURCE, initMethod = "start", destroyMethod = "stop")
  public FlexyPoolDataSource dataSource(
      @Qualifier(DATASOURCE_PROPERTIES) DataSourceProperties properties) {
    final HikariDataSource dataSource = createDataSource(properties, HikariDataSource.class);
    if (StringUtils.hasText(properties.getName())) {
      dataSource.setPoolName(properties.getName());
    }

    return FlexyPoolConfiguration.datasource(dataSource.getPoolName(), dataSource);
  }

  @Primary
  @Bean(name = ENTITY_MANAGER)
  public LocalContainerEntityManagerFactoryBean entityManager(
      @Qualifier(JPA_PROPERTIES) JpaProperties properties,
      @Qualifier(DATASOURCE) DataSource datasource) {
    return this.buildEntityManager(properties, datasource);
  }


  @Primary
  @Bean(name = TRANSACTION_MANAGER)
  public PlatformTransactionManager transactionManager(
      @Qualifier(ENTITY_MANAGER) LocalContainerEntityManagerFactoryBean entityManager) {
    return this.buildTransactionManager(entityManager);
  }


  @Override
  protected String entityPackage() {
    return ENTITY_PACKAGE;
  }

  @Override
  protected String unitName() {
    return UNIT;
  }
}
