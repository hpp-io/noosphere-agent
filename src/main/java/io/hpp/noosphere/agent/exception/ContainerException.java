package io.hpp.noosphere.agent.exception;

public class ContainerException extends RuntimeException {

    private final String containerId;

    public ContainerException(String message, String containerId) {
        super(message);
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }
}
