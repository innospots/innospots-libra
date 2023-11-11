/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.workflow.core.instance.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.hash;

/**
 * @author Raydian
 * @date 2020/11/28
 */
@Getter
@Setter
public class Edge {

    @Schema(title = "instance node edge primary id")
    private Long edgeId;

    @Schema(title = "edge name")
    private String name;

    @Schema(title = "source node instance key")
    private String source;

    @Schema(title = "target node instance key")
    private String target;

    @Schema(title = "anchor of source node")
    private String sourceAnchor;

    @Schema(title = "anchor of target node")
    private String targetAnchor;

    @Schema(title = "type of edge")
    private String type;

    @Schema(title = "node style")
    private Map<String, Object> style;

    @Schema(title = "start point")
    private Map<String, Object> startPoint;

    @Schema(title = "end point")
    private Map<String, Object> endPoint;

    @Schema(title = "data")
    private Map<String, Object> data;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("edgeId=").append(edgeId);
        sb.append(", source='").append(source).append('\'');
        sb.append(", target='").append(target).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge edge = (Edge) o;
        return Objects.equals(source, edge.source) && Objects.equals(target, edge.target) && Objects.equals(sourceAnchor, edge.sourceAnchor) && Objects.equals(targetAnchor, edge.targetAnchor);
    }

    @Override
    public int hashCode() {
        return hash(source, target, sourceAnchor, targetAnchor);
    }
}
