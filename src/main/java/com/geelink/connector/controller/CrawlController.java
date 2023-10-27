package com.geelink.connector.controller;

import com.geelink.connector.model.*;
import com.geelink.connector.service.MetaDataService;
import com.geelink.connector.service.SpiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crawl")
@RequiredArgsConstructor
public class CrawlController {
    private final SpiderService spiderService;
    private final MetaDataService metaDataService;

    @PostMapping("/{collection}")
    public ResponseEntity<UmeResponse> crawler(@RequestBody CrawlerRequest crawlerRequest) {
        spiderService.fetchWeb(crawlerRequest);

        // Do processing with uploaded file data in Service layer
        return new ResponseEntity<>(new UmeResponse.Builder()
                .withUmeCode(UmeCode.SUCCESS)
                .withMessage("操作成功")
                .build(), HttpStatus.OK);
    }

    /* request body:
        {
          "url": "https://www.zj.gov.cn/",
          "depth": 1,
          "processor": "",
          "platform": "windows",
          "chromeVersion": "",
          "numOfPage": 5
        }
     */
    @PostMapping("/metadata")
    public ResponseEntity<UmeResponse> crawlerMetadata(@RequestBody MetaRequest metaRequest) {
        MetaResponse result = metaDataService.fetchMetadata(metaRequest);

        // Do processing with uploaded file data in Service layer
        return new ResponseEntity<>(new UmeResponse.Builder()
                .withUmeCode(UmeCode.SUCCESS)
                .withResult(result)
                .withMessage("操作成功")
                .build(), HttpStatus.OK);
    }
}
