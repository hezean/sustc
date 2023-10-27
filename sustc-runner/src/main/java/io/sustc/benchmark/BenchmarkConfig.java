package io.sustc.benchmark;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Profile("benchmark")
@Configuration
@ConfigurationProperties(prefix = "benchmark")
@Validated
@Data
public class BenchmarkConfig {

    @NotBlank
    private String runId;
}
