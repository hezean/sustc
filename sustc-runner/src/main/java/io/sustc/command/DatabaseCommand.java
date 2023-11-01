package io.sustc.command;

import io.sustc.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
@ConditionalOnBean(DatabaseService.class)
public class DatabaseCommand {

    @Autowired
    private DatabaseService databaseService;

    @ShellMethod(key = "list member", value = "List group members")
    public List<Integer> listGroupMembers() {
        return databaseService.getGroupMembers();
    }

    @ShellMethod(key = "db truncate", value = "Truncate tables")
    public void truncate() {
        databaseService.truncate();
    }

    @ShellMethod(key = "db sum", value = "Demonstrate using DataSource")
    public Integer sumSidJdbcTemplate(int a, int b) {
        return databaseService.sum(a, b);
    }
}
