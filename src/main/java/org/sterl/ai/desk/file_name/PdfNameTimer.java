package org.sterl.ai.desk.file_name;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.sterl.ai.desk.config.AiDeskConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!junit")
@Component
@RequiredArgsConstructor
@Slf4j
public class PdfNameTimer {

    private final FileNameService fileNameService;
    private final AiDeskConfig aiDeskConfig;
    
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!aiDeskConfig.hasSourceDir()) {
            log.warn("Source folder {} does not exists - doing nothing.", aiDeskConfig.getDestinationDir());
            return;
        }

        var files = FileUtils.listFiles(aiDeskConfig.getSourceDir(), new String[] {".pdf", ".PDF"}, true);
        for (File file : files) {
            fileNameService.handlePdf(file, aiDeskConfig.getSourceDir(), aiDeskConfig.getDestinationDir());
        }
    }
}
