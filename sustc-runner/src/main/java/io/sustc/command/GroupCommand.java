package io.sustc.command;

import io.sustc.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
@ConditionalOnBean(GroupService.class)
public class GroupCommand {

    @Autowired
    private GroupService groupService;

    @ShellMethod(key = "group list", value = "List group members")
    public List<Integer> listGroupMembers() {
        return groupService.getGroupMembers();
    }

    @ShellMethod(key = "group sum1", value = "Demonstrate using JdbcTemplate")
    public Integer sumSidJdbcTemplate() {
        return groupService.calcSidSumViaJdbcTemplate();
    }

    @ShellMethod(key = "group sum2", value = "Demonstrate using raw java.sql classes")
    public Integer sumSidJavaSql() {
        return groupService.calcSidSumViaJavaSql();
    }
}
