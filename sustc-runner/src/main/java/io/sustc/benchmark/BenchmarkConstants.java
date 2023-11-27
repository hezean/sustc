package io.sustc.benchmark;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class BenchmarkConstants {

    public static final String IMPORT_DATA_PATH = "import";

    public static final String RAW_DATA_PATH = "u_raw";

    public static final String DANMU_FILENAME = "danmu.csv";

    public static final String USER_FILENAME = "users.csv";

    public static final String VIDEO_FILENAME = "videos.csv";

    public static final int TIMEOUT_MINUTES = 5;
}
