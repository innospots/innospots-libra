package io.innospots.base.execution;

/**
 * @author Smars
 * @date 2024/5/23
 */
public interface IExecutor<I, O> {


    /**
     * @param input
     * @return
     */
    O execute(I input);

    /**
     * global identifier
     *
     * @return
     */
    default String identifier() {
        return "identifier";
    }

    /**
     * executor information
     * @return
     */
    default String info(){
        return identifier();
    }

}
