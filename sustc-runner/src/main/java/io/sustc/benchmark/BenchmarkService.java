package io.sustc.benchmark;

import io.fury.Fury;
import io.fury.ThreadSafeFury;
import io.fury.config.CompatibleMode;
import io.fury.config.Language;
import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DatabaseService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
public class BenchmarkService {

    @Autowired
    private BenchmarkConfig benchmarkConfig;

    @Autowired
    private DatabaseService databaseService;

    @BenchmarkStep(order = 1, timeout = 10, description = "Import data")
    @SneakyThrows
    public BenchmarkResult importData() {
        val dataDir = Paths.get(benchmarkConfig.getDataPath(), BenchmarkConstants.IMPORT_DATA_PATH);

        ThreadSafeFury fury = Fury.builder()
                .requireClassRegistration(false)
                .withLanguage(Language.JAVA)
                .withRefTracking(true)
                .withCompatibleMode(CompatibleMode.COMPATIBLE)
                .withAsyncCompilation(true)
                .buildThreadSafeFury();

        FileInputStream danmuStream = new FileInputStream(
                dataDir.resolve(BenchmarkConstants.DANMU_FILENAME).toString());
        byte[] danmuBytes = danmuStream.readAllBytes();
        danmuStream.close();
        List<DanmuRecord> danmuRecords = (List<DanmuRecord>) fury.deserialize(danmuBytes);

        FileInputStream userSteram = new FileInputStream(dataDir.resolve(BenchmarkConstants.USER_FILENAME).toString());
        byte[] userBytes = userSteram.readAllBytes();
        userSteram.close();
        List<UserRecord> userRecords = (List<UserRecord>) fury.deserialize(userBytes);

        FileInputStream videoStream = new FileInputStream(
                dataDir.resolve(BenchmarkConstants.VIDEO_FILENAME).toString());
        byte[] videoBytes = videoStream.readAllBytes();
        videoStream.close();
        List<VideoRecord> videoRecords = (List<VideoRecord>) fury.deserialize(videoBytes);

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
