package io.sustc.command;

import io.sustc.benchmark.BenchmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;

@ShellComponent
public class BenchmarkCommand {

    @Autowired
    private BenchmarkService benchmarkService;
}
