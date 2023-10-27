package com.geelink.connector.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public abstract class JobModel {
    protected String name;
    private String description;
    protected String collection;
    protected String type;
    protected boolean scheduled;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date startTime;
    private int interval;

    /*

    @ApiModelProperty(value = "properties: {\n" +
            "    type: string, - 子类型 file|data|uri\n" +
            "    data: string[] <- 如果subtype是data，Json格式的数据；\n" +
            "如果subtype是file，指的是本地文件目录\n" +
            "如果是URI，指的是uri地址\n" +
            "    collection: string, - 数据索引的数据集\n" +
            "    batchSize: int, -  每个线程可以处理的文档数\n" +
            "    thread: int - 最多执行的线程数 \n" +
            "}",example =  "{type: file,data: [C:/work/geelink/mgyj.zip],collection: article,batchSize: 100,thread: 1}")
*/
    private Map<String, Object> properties;
}
