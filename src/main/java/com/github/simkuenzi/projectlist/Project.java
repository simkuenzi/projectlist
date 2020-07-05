package com.github.simkuenzi.projectlist;

public class Project {
    private String appName;

    public Project(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppUrl() {
        return "/" + appName;
    }

    public String getGitUrl() {
        return "https://github.com/simkuenzi/" + appName;
    }
}
