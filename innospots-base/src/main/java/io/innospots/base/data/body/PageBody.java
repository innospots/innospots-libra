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

package io.innospots.base.data.body;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Raydian
 * @date 2021/1/30
 */
public class PageBody<T> {

    private List<T> list;

    private final long startTime;

    private int consume;

    private String message;

    private final Pagination pagination;

    public PageBody() {
        pagination = new Pagination();
        startTime = System.currentTimeMillis();
    }

    public long pageSize() {
        return pagination.pageSize;
    }

    public long current() {
        return pagination.current;
    }

    public List<T> getList() {
        return list;
    }

    public void add(T item) {
        if (this.list == null) {
            this.list = new ArrayList<>();
        }
        this.list.add(item);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setList(List<T> list) {
        this.list = list;
        this.consume = Math.toIntExact(System.currentTimeMillis() - startTime);
    }

    public long getStartTime() {
        return startTime;
    }

    public int getConsume() {
        return consume;
    }

    public void end(){
        if(consume==0){
            this.consume = Math.toIntExact(System.currentTimeMillis() - startTime);
        }
    }

    public void setPageSize(Long pageSize) {
        this.pagination.pageSize = pageSize;
    }

    public void setCurrent(Long current) {
        this.pagination.current = current;
    }


    public void setTotal(Long total) {
        this.pagination.total = total;
    }

    public void setTotalPage(Long totalPage) {
        this.pagination.totalPage = totalPage;
    }

    public Pagination getPagination() {
        return pagination;
    }

    @Schema
    public static class Pagination {

        @Schema(title = "current list size")
        private Long pageSize;

        @Schema(title = "current page number")
        private Long current;

        @Schema(title = "total number")
        private Long total;

        @Schema(title = "total page number")
        private Long totalPage;

        public Long getTotalPage() {
            if (this.total == null || this.total == 0 || this.pageSize == null || this.pageSize == 0) {
                return 0L;
            }
            return this.getTotal() % this.pageSize == 0
                    ? this.getTotal() / this.pageSize
                    : this.getTotal() / this.pageSize + 1;
        }

        public Long getPageSize() {
            return pageSize;
        }

        public Long getCurrent() {
            return current;
        }

        public Long getTotal() {
            return total;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("pageSize=").append(pageSize);
            sb.append(", current=").append(current);
            sb.append(", total=").append(total);
            sb.append(", totalPage=").append(totalPage);
            sb.append('}');
            return sb.toString();
        }
    }
}
