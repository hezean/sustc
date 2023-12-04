package io.sustc.benchmark;

import lombok.Builder;
import lombok.Data;

/**
 * Evaluation result of a benchmark task.
 * If any of the fields is null, it means the tasks won't be evaluated by this term.
 */
@Data
@Builder
public class BenchmarkResult {

    private Integer id;

    private Long caseCnt;

    private Long passCnt;

    private Long elapsedTime;
}
