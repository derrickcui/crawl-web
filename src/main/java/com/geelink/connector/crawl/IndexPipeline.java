package com.geelink.connector.crawl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.geelink.connector.util.PageUtil.FIELD_GL_COLLECTION;
import static com.geelink.connector.util.PageUtil.FIELD_ID;


@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexPipeline implements Pipeline {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String collection;
    private final String pipelineAlias;
    private final String outputFolder;

    @Autowired
    public IndexPipeline(String collection, String pipelineAlias, String outputFolder) {
        this.collection = collection;
        this.pipelineAlias = pipelineAlias;
        this.outputFolder = outputFolder;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> payload = new HashMap<>(resultItems.getAll());

        payload.put(FIELD_ID, generateIdByUri(resultItems.getRequest().getUrl()));
        payload.put(FIELD_GL_COLLECTION, collection);

        try {
            String fileName = outputFolder + "/" + getFileName();
            log.info("Writing to file:{}", fileName);
            mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(fileName).toFile(), payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Completed to index url:{}, success:{}", resultItems.getRequest().getUrl());
    }

    private String getFileName() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(date).replaceAll("-", "").replaceAll(" ", "_").replaceAll(":", "").replaceAll("\\.", "_") + ".json";
    }
    private String generateIdByUri(String uri) {
        if ( StringUtils.isNotBlank(uri) ) {
            try {
                return DigestUtils.md5Hex(uri);
            } catch (Exception e) {
                log.error("Failed to convert url:{} to id, use uuid", uri, e);
            }
        }

        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
