package io.sustc.service;

import java.util.List;

public interface GroupService {

    /**
     * Acknowledges the authors of this project.
     *
     * @return a list of group members' student id.
     */
    List<Integer> getGroupMembers();

    /**
     * Sums up all the student id in the group.
     * <p>
     * This method only demonstrates how to use JdbcTemplate in our framework,
     * which is a wrapper of raw java.sql classes.
     *
     * @return the sum of all student id.
     */
    Integer calcSidSumViaJdbcTemplate();

    /**
     * Sums up all the student id in the group.
     * <p>
     * This method only demonstrates how to access database using raw java.sql classes.
     *
     * @return the sum of all student id.
     */
    Integer calcSidSumViaJavaSql();
}
