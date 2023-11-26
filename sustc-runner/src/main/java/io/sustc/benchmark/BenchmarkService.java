package io.sustc.benchmark;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import io.sustc.dto.DanmuRecord;
import io.sustc.dto.UserRecord;
import io.sustc.dto.VideoRecord;
import io.sustc.service.DatabaseService;
import io.sustc.ta.DataGenerateServiceImpl;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Input;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// TA works
import io.sustc.ta.DataGenerateService;
import io.sustc.ta.dto.RawDanmuRecord;
import io.sustc.ta.dto.RawUserRecord;
import io.sustc.ta.dto.RawVideoRecord;


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

    @BenchmarkStep(order = 2, description = "Generate data")
    @SneakyThrows
    public BenchmarkResult generateData() {
        System.setProperty("file.encoding", "UTF-8");
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        val dataDir = Paths.get(benchmarkConfig.getDataPath(), BenchmarkConstants.RAW_DATA_PATH);


//        BOMInputStream danmuStream = new BOMInputStream(
//                        new FileInputStream(dataDir.resolve(BenchmarkConstants.DANMU_FILENAME).toString()),
//                        false, ByteOrderMark.UTF_8
//        );
//        @Cleanup val danmuReader = new BufferedReader(new InputStreamReader(danmuStream));


        @Cleanup val danmuReader = Files.newBufferedReader(dataDir.resolve(BenchmarkConstants.DANMU_FILENAME));
        @Cleanup val userReader = Files.newBufferedReader(dataDir.resolve(BenchmarkConstants.USER_FILENAME));
        @Cleanup val videoReader = Files.newBufferedReader(dataDir.resolve(BenchmarkConstants.VIDEO_FILENAME));

//        System.out.println(danmuReader.readLine());
//        System.out.println(danmuReader.readLine());
//        System.out.println(danmuReader.readLine());
//        System.out.println("我是谁？我在哪？");
//        System.out.println(System.getProperty("file.encoding"));

        val startedTime = System.nanoTime();

//        @Cleanup val new_danmuReader = Files.newBufferedReader(dataDir.resolve(BenchmarkConstants.DANMU_FILENAME));
//        try {
//            File file = new File("test.txt"); // 创建一个文件对象
//            FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8); // 创建一个FileWriter对象
//            writer.write(new_danmuReader.readLine()); // 写入字符串到文件
//            writer.write(new_danmuReader.readLine()); // 写入字符串到文件
//            writer.write(new_danmuReader.readLine()); // 写入字符串到文件
//            writer.write("我是谁？我在哪？"); // 写入字符串到文件
//            writer.close(); // 关闭FileWriter对象
//            String ttt = "成功写入文";
//            System.out.println(ttt);
//        } catch (IOException e) {
//            System.out.println("发生错误。");
//            e.printStackTrace();
//        }

        try {
            DataGenerateService dataGenerateService = new DataGenerateServiceImpl();
            val danmuRecords = dataGenerateService.loadDanmuToBean(danmuReader);
            val userRecords = dataGenerateService.loadUserToBean(userReader);
            val videoRecords = dataGenerateService.loadVideoToBean(videoReader);

            dataGenerateService.loadGenerateSave(danmuRecords, userRecords, videoRecords);
        } catch (Exception e) {
            log.error("Exception encountered during importing data, you may early stop this run", e);
        }
        val finishedTime = System.nanoTime();

        return BenchmarkResult.builder()
                .elapsedTime(finishedTime - startedTime)
                .build();
    }
}
