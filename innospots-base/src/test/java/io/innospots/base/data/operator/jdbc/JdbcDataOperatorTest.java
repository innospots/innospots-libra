package io.innospots.base.data.operator.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @date 2023/8/9
 */
class JdbcDataOperatorTest {

    private JdbcTemplate template;


    void init(){
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("");
        ds.setUrl(""+System.getenv("DB_NAME")+"DB_IP");
        ds.setPassword(System.getenv("DB_PASSWORD"));
        ds.setUsername(System.getenv("DB_USER"));
        //ds.setPassword();
    }


    @Test
    void upsertBatch() {
        System.out.println(System.getenv());
    }
}