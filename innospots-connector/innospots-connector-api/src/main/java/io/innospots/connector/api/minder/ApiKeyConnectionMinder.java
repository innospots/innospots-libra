package io.innospots.connector.api.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.minder.BaseDataConnectionMinder;
import io.innospots.base.data.operator.IOperator;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/2
 */
public class ApiKeyConnectionMinder extends BaseDataConnectionMinder {

    public static final String API_KEY = "api_key";

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }

    @Override
    public String schemaName() {
        return "api-key";
    }

    @Override
    public <Operator extends IOperator> Operator buildOperator() {
        return null;
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        String apiKey = connectionCredential.v(API_KEY);
        return apiKey!=null;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        return true;
    }
}
