package io.sustc.benchmark;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DatabaseService;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Slf4j
public class BenchmarkService {

    @Autowired
    private BenchmarkConfig benchmarkConfig;

    @Autowired
    private DatabaseService databaseService;

    @BenchmarkStep(order = 1, description = "Import data")
    @SneakyThrows
    public BenchmarkResult importData() {
        val dataDir = Paths.get(benchmarkConfig.getDataPath(), BenchmarkConstants.IMPORT_DATA_PATH);

        @Cleanup val danmuReader = Files.newBufferedReader(dataDir.resolve(BenchmarkConstants.DANMU_FILENAME));
        @Cleanup val userReader = Files.newBufferedReader(dataDir.resolve(BenchmarkConstants.USER_FILENAME));
        @Cleanup val videoReader = Files.newBufferedReader(dataDir.resolve(BenchmarkConstants.VIDEO_FILENAME));

        val danmuRecords = new CsvToBeanBuilder<DanmuRecord>(danmuReader)
                .withType(DanmuRecord.class)
                .build()
                .parse();

        val userRecords = new CsvToBeanBuilder<UserRecord>(userReader)
                .withType(UserRecord.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build()
                .parse();

        val videoRecords = new CsvToBeanBuilder<VideoRecord>(videoReader)
                .withType(VideoRecord.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build()
                .parse();

        val startedTime = System.nanoTime();
        try {
            databaseService.importData(danmuRecords, userRecords, videoRecords);
        } catch (Exception e) {
            log.error("Exception encountered during importing data, you may early stop this run", e);
        }
        val finishedTime = System.nanoTime();

        return BenchmarkResult.builder()
                .elapsedTime(finishedTime - startedTime)
                .build();
    }
}
