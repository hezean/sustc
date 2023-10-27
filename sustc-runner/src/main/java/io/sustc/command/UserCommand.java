package io.sustc.command;

import io.sustc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.standard.ShellComponent;

@ShellComponent
@ConditionalOnBean(UserService.class)
public class UserCommand {

    @Autowired
    private UserService userService;
}
