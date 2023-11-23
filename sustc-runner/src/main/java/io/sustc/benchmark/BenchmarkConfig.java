package io.sustc.benchmark;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "benchmark")
@Validated
@Data
public class BenchmarkConfig {

    /**
     * Base path for the data files (import + test cases).
     */
    private String dataPath;

    /**
     * Base path for the generated reports.
     * If not specified, the reports will be generated in the current directory.
     */
    private String reportPath;

    /**
     * Indicator for enabling some additional steps that only run in student mode,
     * e.g., truncate tables.
     */
    private Boolean studentMode = false;
}
