package io.innospots.workflow.node.ai;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/24
 */
public enum LlmExecuteMode {
    STREAM,
    SYNC;

    public boolean isStream(){
        return this == STREAM;
    }
}
