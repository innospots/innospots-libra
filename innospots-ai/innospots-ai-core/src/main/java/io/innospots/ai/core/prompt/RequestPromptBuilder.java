package io.innospots.ai.core.prompt;

import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.json.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/16
 */
@Slf4j
public class RequestPromptBuilder {

    public static Prompt build(BaseRequest<?> itemRequest){
        List<Message> messages = new ArrayList<>();
        if (itemRequest.getBody() instanceof List) {
            Message message = null;
            List<Map> bodyList = (List<Map>) itemRequest.getBody();
            for (Map map : bodyList) {
                fillMessage(messages, map);
            }
        } else if (itemRequest.getBody() instanceof Map) {
            fillMessage(messages, (Map<String, Object>) itemRequest.getBody());
        } else if (itemRequest.getBody() instanceof String) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", itemRequest.getBody());
            item.put("role", "user");
            fillMessage(messages, item);
        }

        return new Prompt(messages);
    }

    private static void fillMessage(List<Message> messages, Map<String, Object> item) {
        Message message = buildMessage(item);
        if (message != null) {
            messages.add(message);
        } else {
            log.warn("message not be built,{}", item);
        }
    }

    private static Message buildMessage(Map<String, Object> item) {
        Message message = null;
        String role = (String) item.get("role");
        if (role == null) {
            role = MessageType.USER.getValue();
        }
        if (MessageType.USER.getValue().equals(role)) {
            Object content = item.get("content");
            if(content instanceof String){
            }else{
                content = JSONUtils.toJsonString(content);
            }
            List<ExecutionResource> resources = (List<ExecutionResource>) item.get("resources");
            List<Media> mediaList = new ArrayList<>();
            if (resources != null) {
                for (ExecutionResource resource : resources) {
                    MediaType mediaType = null;
                    try {
                        mediaType = MediaType.valueOf(resource.getMimeType());
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    }
                    Media media = new Media(mediaType, new FileSystemResource(resource.getLocalUri()));
                    mediaList.add(media);
                }
            }
            message = new UserMessage(String.valueOf(content), mediaList);
        } else if (MessageType.SYSTEM.getValue().equals(role)) {
            String content = (String) item.get("content");
            return new SystemMessage(content);
        } else if (MessageType.ASSISTANT.getValue().equals(role)) {
            String content = (String) item.get("content");
            List tools = (List) item.get("tool_calls");
            AssistantMessage assistantMessage = new AssistantMessage(content);
            return assistantMessage;
        }
        return message;
    }
}
