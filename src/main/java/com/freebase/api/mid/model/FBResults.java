package com.freebase.api.mid.model;

/**
 * Created by Niranjan on 1/25/2016.
 */
public class FBResults {

    private String rel_mid;
    private String subject;
    private String object;

    public String getRel_mid() {
        return rel_mid;
    }

    public void setRel_mid(String rel_mid) {
        this.rel_mid = rel_mid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "FBResults{" +
                "rel_mid='" + rel_mid + '\'' +
                ", subject='" + subject + '\'' +
                ", object='" + object + '\'' +
                '}';
    }
}