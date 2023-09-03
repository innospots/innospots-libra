package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.summary.Product;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingProductFunction extends AbstractMovingFunction {

    private Product product = new Product();

    public MovingProductFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return computeStc(product);
    }
}
