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

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

        if (Boolean.TRUE.equals(benchmarkConfig.getStudentMode())) {
            log.warn("Truncating tables");
            databaseService.truncate();
        }

        val results = Arrays.stream(BenchmarkService.class.getMethods())
                .sequential()
                .filter(method -> method.isAnnotationPresent(BenchmarkStep.class))
                .sorted(Comparator.comparingInt(m -> m.getAnnotation(BenchmarkStep.class).order()))
                .peek(method -> log.info("Step {}: {}",
                        method.getAnnotation(BenchmarkStep.class).order(),
                        StringUtils.defaultIfEmpty(
                                method.getAnnotation(BenchmarkStep.class).description(),
                                method.getName()
                        )
                ))
                .map(method -> {
                    val executor = Executors.newCachedThreadPool();
                    val future = executor.submit(() -> (BenchmarkResult) method.invoke(benchmarkService));
                    try {
                        val res = future.get(method.getAnnotation(BenchmarkStep.class).timeout(), TimeUnit.MINUTES);
                        res.setId(method.getAnnotation(BenchmarkStep.class).order());
                        return res;
                    } catch (TimeoutException e) {
                        log.warn("Task timeout, cancelling it", e);
                        future.cancel(true);
                        if (method.getReturnType().equals(Void.TYPE)) {
                            return null;
                        }
                        return BenchmarkResult.builder()
                                .id(method.getAnnotation(BenchmarkStep.class).order())
                                .passCnt(0L)
                                .elapsedTime(TimeUnit.MINUTES.toNanos(method.getAnnotation(BenchmarkStep.class).timeout()))
                                .build();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        executor.shutdownNow();
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
        @Cleanup val writer = new OutputStreamWriter(
                Files.newOutputStream(reportFile.toPath()),
                StandardCharsets.UTF_8
        );
        val beanToCsv = new StatefulBeanToCsvBuilder<BenchmarkResult>(writer)
                .withApplyQuotesToAll(false)
                .build();
        beanToCsv.write(results);
    }
}
