/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.base.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.innospots.base.i18n.LocaleContext;
import io.innospots.base.json.deserializer.CustomerDateDeserializer;
import io.innospots.base.json.deserializer.CustomerLocalDateDeserializer;
import io.innospots.base.json.deserializer.CustomerLocalDateTimeDeserializer;
import io.innospots.base.json.deserializer.CustomerLocalTimeDeserializer;
import io.innospots.base.json.serializer.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * json utils
 */
public class JSONUtils {

    private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

    /**
     * can use static singleton, inject: just make sure to reuse!
     */
    private static final ObjectMapper OBJECT_MAPPER = customMapper();


    private JSONUtils() {
        throw new UnsupportedOperationException("Construct JSONUtils");
    }

    public static ObjectMapper customMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat(LocaleContext.getDefaultLocalContext().getDateFormat()));
        objectMapper.setLocale(LocaleContext.getDefaultLocalContext().getLocale());
        objectMapper.setTimeZone(LocaleContext.getDefaultLocalContext().getTimeZone());
        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class,CustomerLocalDateTimeSerializer.getInstance());
        module.addSerializer(LocalDate.class,CustomerLocalDateSerializer.getInstance());
        module.addSerializer(Date.class,CustomerDateSerializer.getInstance());
        module.addSerializer(LocalTime.class,CustomerLocalTimeSerializer.getInstance());
        module.addSerializer(java.sql.Date.class,CustomerSqlDateSerializer.getInstance());
        module.addDeserializer(LocalDateTime.class, CustomerLocalDateTimeDeserializer.getInstance())
                .addDeserializer(LocalTime.class, CustomerLocalTimeDeserializer.getInstance())
                .addDeserializer(LocalDate.class, CustomerLocalDateDeserializer.getInstance())
                .addDeserializer(Date.class, CustomerDateDeserializer.getInstance());
        objectMapper.registerModule(module);
        objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS,false);
        objectMapper.configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,true);
        objectMapper.configure(ALLOW_SINGLE_QUOTES,true);
        objectMapper.configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL,true);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES,false);
        objectMapper.configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature(),true);

        return objectMapper;
    }

    public static ObjectMapper mapper() {
        return OBJECT_MAPPER;
    }

    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    public static JsonNode toJsonNode(Object obj) {
        return OBJECT_MAPPER.valueToTree(obj);
    }

    /**
     * json representation of object
     *
     * @param object  object
     * @param feature feature
     * @return object to json string
     */
    public static String toJsonString(Object object, SerializationFeature feature) {
        try {
            ObjectWriter writer = OBJECT_MAPPER.writer(feature);
            return writer.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("object to json exception!", e);
        }

        return null;
    }

    /**
     * json representation of object
     *
     * @param object object
     * @return object to json string
     */
    public static String toJsonStringPretty(Object object) {
        try {

            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            logger.error("object to json exception!", e);
        }

        return null;
    }

    public static Map<String, Object> objectToMap(Object object) {
        return OBJECT_MAPPER.convertValue(object, LinkedHashMap.class);
    }

    public static List<String> objectToStrList(Object object) {
        if (object == null) {
            return Collections.emptyList();
        }

        try {
            CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
            return OBJECT_MAPPER.readValue(String.valueOf(object), listType);
        } catch (Exception e) {
            logger.error("parse list exception!", e);
        }

        return Collections.emptyList();
    }

    /**
     * This method deserializes the specified Json into an object of the specified class. It is not
     * suitable to use if the specified class is a generic type since it will not have the generic
     * type information because of the Type Erasure feature of Java. Therefore, this method should not
     * be used if the desired type is a generic type. Note that this method works fine if the any of
     * the fields of the specified object are generics, just the object itself should not be a
     * generic type.
     *
     * @param json  the string from which the object is to be deserialized
     * @param clazz the class of T
     * @param <T>   T
     * @return an object of type T from the string
     * classOfT
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("parse object exception!", e);
        }
        return null;
    }

    public static <T> T parseObject(Map<String,Object> map, Class<T> clazz) {
        return parseObject(toJsonString(map),clazz);
    }

    public static <T> T parseObject(String json, TypeReference<T> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (Exception e) {
            logger.error("parse object exception!", e);
        }
        return null;
    }

    /**
     * deserialize
     *
     * @param src   byte array
     * @param clazz class
     * @param <T>   deserialize type
     * @return deserialize type
     */
    public static <T> T parseObject(byte[] src, Class<T> clazz) {
        if (src == null) {
            return null;
        }
        String json = new String(src, UTF_8);
        return parseObject(json, clazz);
    }

    /**
     * json to list
     *
     * @param json  json string
     * @param clazz class
     * @param <T>   T
     * @return list
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }

        try {

            CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return OBJECT_MAPPER.readValue(json, listType);
        } catch (Exception e) {
            logger.error("parse list exception!", e);
        }

        return Collections.emptyList();
    }

    /**
     * json to list
     *
     * @param json  json string
     * @param clazz class
     * @return list
     */
    public static List<Map<String, Object>> toMapList(String json, Class<Map> clazz) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }

        try {

            CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return OBJECT_MAPPER.readValue(json, listType);
        } catch (Exception e) {
            logger.error("parse list exception!", e);
        }

        return Collections.emptyList();
    }

    /**
     * json to list
     *
     * @param json  json string
     * @param clazz class
     * @param <T>   T
     * @return list
     */
    public static <T> Set<T> toSet(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptySet();
        }

        try {

            CollectionType setType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, clazz);
            return OBJECT_MAPPER.readValue(json, setType);
        } catch (Exception e) {
            logger.error("parse list exception!", e);
        }

        return Collections.emptySet();
    }

    /**
     * check json object valid
     *
     * @param json json
     * @return true if valid
     */
    public static boolean checkJsonValid(String json) {

        if (StringUtils.isEmpty(json)) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (IOException e) {
            logger.error("check json object valid exception!", e);
        }

        return false;
    }


    /**
     * json to map
     * <p>
     * {@link #toMap(String, Class, Class)}
     *
     * @param json json
     * @return json to map
     */
    public static Map<String, String> toStrMap(String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return toMap(json, String.class, String.class);
        } catch (Exception e) {
            logger.error("json to map exception!", e);
        }
        return null;
    }

    public static Map<String, Object> toMap(String json) {
        return toMap(json, String.class, Object.class);
    }

    /**
     * json to map
     *
     * @param json   json
     * @param classK classK
     * @param classV classV
     * @param <K>    K
     * @param <V>    V
     * @return to map
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> classK, Class<V> classV) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<LinkedHashMap<K, V>>() {
            });
        } catch (Exception e) {
            logger.error("json to map exception!, {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * object to json string
     *
     * @param object object
     * @return json string
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Object json deserialization exception.", e);
        }
    }

    /**
     * serialize to json byte
     *
     * @param obj object
     * @param <T> object type
     * @return byte array
     */
    public static <T> byte[] toJsonByteArray(T obj) {
        if (obj == null) {
            return null;
        }
        String json = "";
        try {
            json = toJsonString(obj);
        } catch (Exception e) {
            logger.error("json serialize exception.", e);
        }

        return json.getBytes(UTF_8);
    }

    public static ObjectNode parseObject(String text) {
        try {
            return (ObjectNode) OBJECT_MAPPER.readTree(text);
        } catch (Exception e) {
            throw new RuntimeException("String json deserialization exception.", e);
        }
    }

    public static ArrayNode parseArray(String text) {
        try {
            return (ArrayNode) OBJECT_MAPPER.readTree(text);
        } catch (Exception e) {
            throw new RuntimeException("Json deserialization exception.", e);
        }
    }


    /**
     * json serializer
     */
    /*
    public static class JsonDataSerializer extends JsonSerializer<String> {

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeRawValue(value);
        }

    }

     */

    /**
     * json data deserializer
     */
    /*
    public static class JsonDataDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node instanceof TextNode) {
                return node.asText();
            } else {
                return node.toString();
            }
        }

    }

     */
}
