package com.shaoteemo.service;

public interface ActivitySystemService {

    boolean shutdownProcessEngine();

    boolean processEngineStatus();

    boolean startProcessEngine();

    boolean restartProcessEngine();

    Object queryDeploymentProcessCount();

    Object queryStartedProcessInstance();

}
