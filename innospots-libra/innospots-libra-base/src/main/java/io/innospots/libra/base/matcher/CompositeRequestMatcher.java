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

package io.innospots.libra.base.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Smars
 * @date 2021/2/16
 */
public class CompositeRequestMatcher implements RequestMatcher {


    private static final Logger logger = LoggerFactory.getLogger(CompositeRequestMatcher.class);

    private final List<RequestMatcher> requestMatchers;


    public CompositeRequestMatcher(List<RequestMatcher> requestMatchers) {
        Assert.notEmpty(requestMatchers, "requestMatchers must contain a value");
        if (requestMatchers.contains(null)) {
            throw new IllegalArgumentException("requestMatchers cannot contain null values");
        } else {
            this.requestMatchers = requestMatchers;
        }
    }

    public CompositeRequestMatcher(RequestMatcher... requestMatchers) {
        this(Arrays.asList(requestMatchers));
    }


    @Override
    public boolean matches(HttpServletRequest httpServletRequest) {
        Iterator<RequestMatcher> matcherIterator = this.requestMatchers.iterator();
        RequestMatcher matcher;
        do {
            if (!matcherIterator.hasNext()) {
                //logger.debug("No matches found");
                return false;
            }
            matcher = matcherIterator.next();
            if (logger.isDebugEnabled()) {
//                logger.debug("Trying to match using {} ",matcher);
            }
        } while (!matcher.matches(httpServletRequest));

//        logger.debug("matched");
        return true;
    }
}
