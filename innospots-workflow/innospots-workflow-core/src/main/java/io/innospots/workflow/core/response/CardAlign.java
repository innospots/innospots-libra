package io.innospots.workflow.core.response;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/4
 */
public enum CardAlign {

    LEFT("left"),
    RIGHT("right"),
    CENTER("center");

    private String align;

    CardAlign(String align) {
        this.align = align;
    }

    public String align() {
        return align;
    }
}
