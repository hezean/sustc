package io.sustc.benchmark;

import com.opencsv.bean.StatefulBeanToCsvBuilder;
import io.sustc.service.DatabaseService;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.ShellApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Profile("benchmark")
@Component
@Slf4j
public class BenchmarkRunner implements ShellApplicationRunner {

    @Autowired
    private BenchmarkConfig benchmarkConfig;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private BenchmarkService benchmarkService;

    @Override
    @SneakyThrows
    public void run(ApplicationArguments args) {
        log.info("Starting benchmark for group {}", databaseService.getGroupMembers());
        log.info("{}", benchmarkConfig);

        if (benchmarkConfig.getStudentMode()) {
            log.warn("Truncating tables");
            databaseService.truncate();
        }

        val results = Arrays.stream(benchmarkService.getClass().getMethods())
                .sequential()
                .filter(method -> method.isAnnotationPresent(BenchmarkStep.class))
                .sorted((m1, m2) -> {
                    val s1 = m1.getAnnotation(BenchmarkStep.class).order();
                    val s2 = m2.getAnnotation(BenchmarkStep.class).order();
                    return Integer.compare(s1, s2);
                })
                .peek(method -> log.info("Step {}: {}",
                        method.getAnnotation(BenchmarkStep.class).order(),
                        StringUtils.defaultIfEmpty(
                                method.getAnnotation(BenchmarkStep.class).description(),
                                method.getName()
                        )
                ))
                .map(method -> {
                    try {
                        return (BenchmarkResult) method.invoke(benchmarkService);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .peek(result -> log.info("{}", result))
                .collect(Collectors.toList());

        val sid = databaseService.getGroupMembers().stream().map(String::valueOf).collect(Collectors.joining("_"));
        val reportFile = Paths.get(ObjectUtils.defaultIfNull(benchmarkConfig.getReportPath(), ""))
                .resolve(String.format("benchmark-%s-%d.csv", sid, System.currentTimeMillis()))
                .toAbsolutePath()
                .toFile();

        log.info("Benchmark finished, writing report to file: {}", reportFile);
        @Cleanup val writer = new FileWriter(reportFile);
        val beanToCsv = new StatefulBeanToCsvBuilder<BenchmarkResult>(writer)
                .withApplyQuotesToAll(false)
                .build();
        beanToCsv.write(results);
    }
}
