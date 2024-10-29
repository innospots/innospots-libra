package io.innospots.script.base;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/7/24
 */
public enum OutputMode {
    /**
     * set node result to the field
     */
    FIELD,
    /**
     * output current node result and input data
     */
    PAYLOAD,
    /**
     * only output current node result
     */
    OVERWRITE,

    /**
     * output to stream
     */
    STREAM,

    LOG;
}
