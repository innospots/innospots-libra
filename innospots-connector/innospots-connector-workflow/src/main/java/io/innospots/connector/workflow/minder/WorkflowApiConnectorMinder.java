package io.innospots.connector.workflow.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.http.HttpConstant;
import io.innospots.base.connector.http.HttpDataConnectionMinder;
import io.innospots.base.connector.schema.reader.ISchemaRegistryReader;
import io.innospots.base.utils.http.HttpClientBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.innospots.base.connector.http.HttpConstant.KEY_TOKEN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/31
 */
public class WorkflowApiConnectorMinder extends HttpDataConnectionMinder {

    public static final String KEY_WORKFLOW_API = "workflow.address";
    public static final String KEY_WORKFLOW_TEST_API = "workflow.test.address";

    @Override
    public void initialize(ISchemaRegistryReader schemaRegistryReader, ConnectionCredential connectionCredential) {
        super.initialize(schemaRegistryReader, connectionCredential);
        String isTest = connectionCredential.v("is_test");
        String workflowUrl = System.getProperty(KEY_WORKFLOW_TEST_API);
        String path = connectionCredential.v("path");
        if (isTest != null & Boolean.parseBoolean(isTest) && workflowUrl != null) {
            connectionCredential.config(HttpConstant.HTTP_API_URL, workflowUrl + path);
        }
        workflowUrl = System.getProperty(KEY_WORKFLOW_API);
        if (workflowUrl != null) {
            connectionCredential.config(HttpConstant.HTTP_API_URL, workflowUrl + path);
        }
    }


    @Override
    protected Supplier<Map<String, String>> headers() {
        return () -> {
            HashMap<String, String> headers = new HashMap<>();
            String token = this.connectionCredential.v(KEY_TOKEN);
            if (token != null) {
                HttpClientBuilder.fillBearerAuthHeader(token, headers);
            }
            return headers;
        };
    }


    @Override
    public String schemaName() {
        return "workflow_schema";
    }
}
