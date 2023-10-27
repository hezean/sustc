package io.sustc.benchmark;

import io.sustc.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.ShellApplicationRunner;
import org.springframework.stereotype.Component;

@Profile("benchmark")
@Component
@Slf4j
public class BenchmarkRunner implements ShellApplicationRunner {

    @Autowired
    private BenchmarkConfig benchmarkConfig;

    @Autowired
    private GroupService groupService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting benchmark for group {}", groupService.getGroupMembers());
        log.info("{}", benchmarkConfig);

        // TODO: benchmark script
    }
}
