package io.innospots.base.data.request;

/**
 * @author Smars
 * @date 2023/10/28
 */
public class SimpleRequest extends BaseRequest<String> {

    public SimpleRequest(String body) {
        this.body = body;
    }


    public SimpleRequest(String credentialKey,String body) {
        this.credentialKey = credentialKey;
        this.body = body;
    }

    public SimpleRequest() {
    }
}
