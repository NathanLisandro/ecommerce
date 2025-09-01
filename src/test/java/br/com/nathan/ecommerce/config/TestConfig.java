package br.com.nathan.ecommerce.config;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;

@Configuration
@Profile("test")
@EnableJpaAuditing
public class TestConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public IDatabaseConnection dbUnitDatabaseConnection() throws Exception {
        DatabaseDataSourceConnection connection = new DatabaseDataSourceConnection(dataSource);
        connection.getConfig().setProperty("http://www.dbunit.org/properties/datatypeFactory", new H2DataTypeFactory());
        connection.getConfig().setFeature("http://www.dbunit.org/features/allowEmptyFields", true);
        return connection;
    }
}
