package io.sustc.service.impl;

import io.sustc.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * It's important to mark your implementation class with {@link Service} annotation.
 * As long as the class is annotated and implements the corresponding interface, you can place it under any package.
 */
@Service
@Slf4j
public class GroupServiceImpl implements GroupService {

    /**
     * {@link JdbcTemplate} is a higher level wrapper of {@link DataSource}.
     * <p>
     * Marking a field with {@link Autowired} annotation enables our framework to automatically
     * provide you a well-configured instance of {@link JdbcTemplate}.
     * Learn more: <a href="https://www.baeldung.com/spring-dependency-injection">Dependency Injection</a>
     */
    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Using raw {@link DataSource} is also possible.
     * If you prefer this way, you won't need to autowire a {@link JdbcTemplate} in your service implementations.
     */
    @Autowired
    private DataSource dataSource;

    @Override
    public List<Integer> getGroupMembers() {
        throw new UnsupportedOperationException("TODO: replace this with your own student id");
        // return Arrays.asList(12210000, 12210001, 12210002);
    }

    /*
     * The following code is just a quick example of using jdbc.
     * Practically, the code interacts with database is usually written in a DAO layer.
     *
     * Reference: [Data Access Object pattern](https://www.baeldung.com/java-dao-pattern)
     */

    public Integer calcSidSumViaJdbcTemplate() {
        List<Integer> sidList = getGroupMembers();

        String placeholder = String.join(",", Collections.nCopies(sidList.size(), "?"));
        String sql = String.format("SELECT SUM(sid) FROM UNNEST(ARRAY[%s]) AS sid", placeholder);
        log.info("SQL: {}", sql);

        return jdbc.queryForObject(sql, Integer.class, sidList.toArray());
    }

    public Integer calcSidSumViaJavaSql() {
        List<Integer> sidList = getGroupMembers();

        String placeholder = String.join(",", Collections.nCopies(sidList.size(), "?"));
        String sql = String.format("SELECT SUM(sid) FROM UNNEST(ARRAY[%s]) AS sid", placeholder);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < sidList.size(); i++) {
                stmt.setInt(i + 1, sidList.get(i));
            }
            log.info("SQL: {}", stmt);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
