package com.geelink.connector.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job extends JobModel {
    protected String id;
    protected String namespace;
    private String status;
    @JsonProperty("_gl_created_dt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date createdOn;
    private String createdBy;
    @JsonProperty("_gl_updated_dt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date updatedOn;
    private String updatedBy;
    private int sequence;
    @Override
    public int hashCode() {
        return this.collection.hashCode() + Optional.ofNullable(this.getId()).map(t -> t.hashCode()).orElse(new Random().hashCode());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;

        if (!(obj instanceof Job))
            return false;

        Job other = (Job) obj;

        return this.collection.equals(((Job) obj).collection) && this.getId().equals(other.getId());
    }
}
