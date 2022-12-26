package com.example.ali;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@ConfigurationProperties(prefix = "aliyun")
public class AliyunResource {

    private String accessKeyID;
    private String accessKeySecret;
    private List<String> imgScenarios;
    private List<String> textScenarios;

    public String getAccessKeyID() {
        return accessKeyID;
    }

    public void setAccessKeyID(String accessKeyID) {
        this.accessKeyID = accessKeyID;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public List<String> getImgScenarios() {
        return imgScenarios;
    }

    public void setImgScenarios(List<String> imgScenarios) {
        this.imgScenarios = imgScenarios;
    }

    public List<String> getTextScenarios() {
        return textScenarios;
    }

    public void setTextScenarios(List<String> textScenarios) {
        this.textScenarios = textScenarios;
    }
}
