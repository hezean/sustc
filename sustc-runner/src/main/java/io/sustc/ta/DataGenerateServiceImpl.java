package io.sustc.ta;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import io.sustc.ta.dto.RawDanmuRecord;
import io.sustc.ta.dto.RawUserRecord;
import io.sustc.ta.dto.RawVideoRecord;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.util.List;


/**
 * It's important to mark your implementation class with {@link Service} annotation.
 * As long as the class is annotated and implements the corresponding interface, you can place it under any package.
 */
@Service
@Slf4j
public class DataGenerateServiceImpl implements DataGenerateService {

    /**
     * Getting a {@link DataSource} instance from the framework, whose connections are managed by HikariCP.
     * <p>
     * Marking a field with {@link Autowired} annotation enables our framework to automatically
     * provide you a well-configured instance of {@link DataSource}.
     * Learn more: <a href="https://www.baeldung.com/spring-dependency-injection">Dependency Injection</a>
     */
    @Autowired
    private DataSource dataSource;

    /**
     * Special cases:
     * BV13b4y1b7ca,118638,34.266,"?"";[\]‘、】‘=']\0-O\"
     */
    @Override
    public List<RawDanmuRecord> loadDanmuToBean(BufferedReader danmuReader) throws IOException, CsvDataTypeMismatchException {
//        String line = null;
//        List<RawDanmuRecord> danmuRecord = new ArrayList<>();
//        Pattern PATTERN = Pattern.compile("\\('(?<mid>\\d+)', (?<ts>\\d+)\\)");
//
//        while ((line = danmuReader.readLine()) != null) {
//            String[] split = line.split(",");
//            RawDanmuRecord csvFile = new RawDanmuRecord();
//            val matcher = PATTERN.matcher(line);
//            if (!matcher.find()) {
//                throw new CsvDataTypeMismatchException(line, Long.class);
//            }
//
//            csvFile.setName(splitResult(split[0]));
//            csvFile.setTitle(splitResult(split[1]));
//            csvFile.setNumber(splitResult(split[2]));
//            csvFile.setType(splitResult(split[3]));
//            csvFile.setPersonnel(splitResult(split[4]));
//            csvFile.setTime(splitResult(split[5]));
//            danmuRecord.add(csvFile);
//        }
//        System.out.println("???");
//        System.out.println(danmuReader.readLine());
//        return null;
        RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
        CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(danmuReader)
                .withCSVParser(rfc4180Parser);
        val danmuRecords = new CsvToBeanBuilder<RawDanmuRecord>(csvReaderBuilder.build())
                .withType(RawDanmuRecord.class)
                .build()
                .parse();
        return danmuRecords;
    }

    @Override
    public List<RawUserRecord> loadUserToBean(BufferedReader userReader) {
        RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
        CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(userReader)
                .withCSVParser(rfc4180Parser);
        val userRecords = new CsvToBeanBuilder<RawUserRecord>(csvReaderBuilder.build())
                .withType(RawUserRecord.class)
                .build()
                .parse();
        return userRecords;
    }

    @Override
    public List<RawVideoRecord> loadVideoToBean(BufferedReader videoReader) {
        RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
        CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(videoReader)
                .withCSVParser(rfc4180Parser);
        val videoRecords = new CsvToBeanBuilder<RawVideoRecord>(csvReaderBuilder.build())
                .withType(RawVideoRecord.class)
                .build()
                .parse();
        return videoRecords;
    }

    @Override
    public  long loadGenerateSave(
            List<RawDanmuRecord> danmuRecords,
            List<RawUserRecord> userRecords,
            List<RawVideoRecord> videoRecords
    ) {

        try {
            File file = new File("test.txt"); // 创建一个文件对象
            FileWriter writer = new FileWriter(file); // 创建一个FileWriter对象

            int count = 0;
            for (RawDanmuRecord s: danmuRecords) {
                count ++;
                String str = s.getBv() + " " + s.getMid() + " " +  s.getTime() + " " +s.getContent() + "\n";
                writer.write(str);
                if (count > 10) {
                    break;
                }
            }
            count = 0;
            writer.write("\n");
            for (RawUserRecord s: userRecords) {
                count ++;
                String str = s.getName() + " " + s.getSign() + " " +  s.getSex() + " " + s.getIdentity() + " " +s.getFollowing().length + "\n";
                writer.write(str);
                if (count > 10) {
                    break;
                }
            }
            count = 0;
            writer.write("\n");
//            for ( :)
            writer.write("\n");

            for (RawVideoRecord s: videoRecords) {
                count ++;
                System.out.println(s.getDescription() );
                String str = s.getBv() + " " + s.getTitle() + " " +  s.getDescription() + " " + s.getOwnerMid() +  "\n";
                writer.write(str);
                if (count > 4) {
                    break;
                }
            }

            writer.close(); // 关闭FileWriter对象
        } catch (IOException e) {
            System.out.println("发生错误。");
            e.printStackTrace();
        }

        return danmuRecords.size();
    }

}
