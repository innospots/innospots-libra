package io.innospots.connector.ai.aliyun.operator;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
class AliImageGenOperatorTest {


    @Test
    void test(){
        Map<String,Object> options = new HashMap<>();
        AliImageGenOperator imageGenOperator = new AliImageGenOperator(System.getenv("API_KEY"),options);
        Map<String,Object> body = new HashMap<>();

        body.put("prompt","大学女生，高分辨率，精致的脸部比例，精细的裙子，校园");
        body.put("n",1);
        body.put("size","1024*1024");
        DataBody dataBody = imageGenOperator.execute(build(body));
        System.out.println(dataBody.getBody());
    }

    BaseRequest build(Map<String,Object> body){
        BaseRequest request = new BaseRequest<>();
        request.setTargetName("wanx-v1");
        request.setBody(body);
        return request;
    }

    @Test
    void testRefImage(){
        Map<String,Object> options = new HashMap<>();
        AliImageGenOperator imageGenOperator = new AliImageGenOperator(System.getenv("API_KEY"),options);
        Map<String,Object> body = new HashMap<>();
        body.put("prompt","大学女生，校园，写实，照片风格");
        body.put("refImage","https://n.sinaimg.cn/ent/4_img/upload/9b7b89c5/330/w2040h3090/20210410/93dd-knqqqmu4068606.jpg");
        body.put("n",1);
        body.put("size","1024*1024");
        DataBody dataBody = imageGenOperator.execute(build(body));
        System.out.println(dataBody.getBody());
    }

    @Test
    void testCosplayImage(){
        Map<String,Object> options = new HashMap<>();
        AliImageGenOperator imageGenOperator = new AliImageGenOperator(System.getenv("API_KEY"),options);
        Map<String,Object> body = new HashMap<>();
//        body.put("n",1);
//        body.put("size","1024*1024");
        Map<String,Object> extraInputs = new HashMap<>();
        extraInputs.put("model_index",1);
        extraInputs.put("template_image_url","https://huarong123.oss-cn-hangzhou.aliyuncs.com/image/%E9%A3%8E%E6%A0%BC%E5%8F%82%E8%80%83%E5%9B%BE-%E8%A3%81%E5%89%AA.jpg");
        extraInputs.put("face_image_url","https://huarong123.oss-cn-hangzhou.aliyuncs.com/image/cosplay-%E5%8E%9F%E5%9B%BE-%E5%A5%B3.jpg");
        body.put("extraInputs",extraInputs);
        BaseRequest request = new BaseRequest();
        request.setTargetName("wanx-style-cosplay-v1");
        request.setBody(body);
        DataBody dataBody = imageGenOperator.execute(request);
        System.out.println(dataBody.getBody());
    }

}