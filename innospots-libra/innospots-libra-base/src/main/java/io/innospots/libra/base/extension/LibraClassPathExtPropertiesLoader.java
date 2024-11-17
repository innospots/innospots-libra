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

package io.innospots.libra.base.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.innospots.libra.base.menu.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/12/6
 */
@Slf4j
public class LibraClassPathExtPropertiesLoader implements LibraExtPropertiesLoader {

    public static final String KERNEL_APP_KEY = "innospots-libra-extension-kernel";
    public static final String APPLICATION_META_FILE = "META-INF/innospots-extension-meta.json";


    private static final LibraClassPathExtPropertiesLoader INSTANCE = new LibraClassPathExtPropertiesLoader();

    private boolean loaded;

    private final Map<String, LibraExtensionProperties> appCache = new HashMap<>();

    private final Map<String, ResourceItem> resourceItemCache = new HashMap<>();

    public static List<LibraExtensionProperties> loadFromClassPath() {
        return INSTANCE.load(true);
    }

    public static Collection<LibraExtensionProperties> getLibraExtensions() {
        return INSTANCE.appCache.values();
    }

    public static LibraExtensionProperties getLibraKernelProperties() {
        return getLibraExtProperties(KERNEL_APP_KEY);
    }

    public static List<ResourceItem> getAdminDefaultMenuItems() {
        return getLibraKernelProperties().getModules().stream()
                .filter(BaseItem::isAdminDefault)
                .collect(Collectors.toList());
    }

    public static Map<String, List<OptElement>> listOptElements(Set<OptElement.UriMethod> excludeMethods, Set<ItemType> itemTypes) {
        return listOptElements(Collections.emptySet(), excludeMethods, itemTypes);
    }

    public static Map<String, List<OptElement>> listOptElements(Set<String> optItemKeys) {
        return listOptElements(optItemKeys, Collections.emptySet(), Collections.emptySet());
    }

    /**
     * key: menuItem key, value: the collection of opt items in the menuItem
     *
     * @param optItemKeys
     * @return
     */
    public static Map<String, List<OptElement>> listOptElements(Set<String> optItemKeys, Set<OptElement.UriMethod> excludeMethods, Set<ItemType> itemTypes) {
        Map<String, List<OptElement>> optElementMap = new HashMap<>();
        if (INSTANCE.appCache.isEmpty()) {
            loadFromClassPath();
        }
        for (LibraExtensionProperties libraAppProperties : INSTANCE.appCache.values()) {
            for (ResourceItem resourceItem : libraAppProperties.getModules()) {
                fillOptElement(resourceItem, optElementMap, optItemKeys, itemTypes, excludeMethods);
            }
        }
        return optElementMap;
    }

    private static void fillOptElement(ResourceItem resourceItem, Map<String, List<OptElement>> optElementMap,
                                       Set<String> optItemKeys,
                                       Set<ItemType> excludeItemTypes,
                                       Set<OptElement.UriMethod> excludeMethods) {
        if (CollectionUtils.isNotEmpty(resourceItem.getOpts())) {
            List<OptElement> list = resourceItem.getOpts().stream()
                    .filter(optElement ->
                            (CollectionUtils.isEmpty(optItemKeys) || optItemKeys.contains(optElement.getItemKey()))
                                    && (excludeMethods.isEmpty() || !excludeMethods.contains(optElement.getMethod()))
                                    && (excludeItemTypes.isEmpty() || !excludeItemTypes.contains(optElement.getItemType()))
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                optElementMap.put(resourceItem.getItemKey(), list);
            }
        }
        if (CollectionUtils.isNotEmpty(resourceItem.getItems())) {
            for (ResourceItem item : resourceItem.getItems()) {
                fillOptElement(item, optElementMap, optItemKeys, excludeItemTypes, excludeMethods);
            }
        }
    }

    public static ResourceItem getResourceItem(String itemKey) {
        if (INSTANCE.resourceItemCache.isEmpty()) {
            loadFromClassPath();
        }
        return INSTANCE.resourceItemCache.get(itemKey);
    }

    public static Collection<ResourceItem> getAllResourceItems() {
        if (INSTANCE.resourceItemCache.isEmpty()) {
            loadFromClassPath();
        }
        return INSTANCE.resourceItemCache.values();
    }

    /**
     * load application properties from innospots-extension-metadata.json in the classpath.
     *
     * @return
     */
    @Override
    public List<LibraExtensionProperties> load(boolean loadOptElement) {
        if (loaded) {
            return new ArrayList<>(this.appCache.values());
        }
        loaded = true;
        List<LibraExtensionProperties> props = new ArrayList<>();
        try {
            Enumeration<URL> urls = this.getClass().getClassLoader().getResources(APPLICATION_META_FILE);
            while (urls.hasMoreElements()) {
                URL appUrl = urls.nextElement();
                LibraExtensionProperties libraApp = load(appUrl);
                if (libraApp != null) {
                    scanMenuItem(libraApp, loadOptElement);
                    props.add(libraApp);
                    appCache.put(libraApp.getExtKey(), libraApp);
                    fillItemCache(libraApp.getModules());
                }

            }//end while

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return props;
    }

    private void fillItemCache(List<ResourceItem> resourceItems) {
        for (ResourceItem resourceItem : resourceItems) {
            resourceItemCache.put(resourceItem.getItemKey(), resourceItem);
            if (CollectionUtils.isNotEmpty(resourceItem.getItems())) {
                fillItemCache(resourceItem.getItems());
            }
        }
    }


    public static LibraExtensionProperties loadLibraExtProperties(String appKey, boolean loadOptElement) {
        Enumeration<URL> urls = null;
        LibraExtensionProperties libraAppProperties = null;
        try {
            urls = LibraClassPathExtPropertiesLoader.class.getClassLoader().getResources(APPLICATION_META_FILE);
            while (urls.hasMoreElements()) {
                URL appUrl = urls.nextElement();
                libraAppProperties = load(appUrl);
                if (libraAppProperties != null && libraAppProperties.getExtKey().equals(appKey)) {
                    scanMenuItem(libraAppProperties, loadOptElement);
                    break;
                }

            }//end while
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return libraAppProperties;
    }

    public static LibraExtensionProperties getLibraExtProperties(String extKey) {
        if (INSTANCE.appCache.isEmpty()) {
            loadFromClassPath();
        }
        return INSTANCE.appCache.get(extKey);
    }

    private static LibraExtensionProperties load(URL appUrl) {
        LibraExtensionProperties libraExtProperties = null;
        try {
            log.debug("load app properties url: {}", appUrl);
            ObjectMapper objectMapper = new ObjectMapper();
            libraExtProperties = objectMapper.readValue(appUrl, LibraExtensionProperties.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return libraExtProperties;
    }

    /**
     * scan basePackages MenuOptTag and MenuItemTag, and add to menuItem opts
     *
     * @param libraExtProperties
     */
    public static void scanMenuItem(LibraExtensionProperties libraExtProperties, boolean loadOptElement) {
        //add menu item opts
        Object[] packages = libraExtProperties.getBasePackages();
        if (ArrayUtils.isEmpty(packages)) {
            return;
        }
        //TODO 调整匹配方式，一个菜单对应一个ResourceItem，一个菜单中包含多个Controller的ModuleMenuOperation
        Map<String, ResourceItem> menuItemMap = new HashMap<>();
        fillMenuMap(menuItemMap, libraExtProperties.getModules(),libraExtProperties.getExtKey());
        List<ModuleMenuOperation> menuTags = MenuTagScanner.scan(packages);

        for (ModuleMenuOperation menuTag : menuTags) {
            String itemKey = menuTag.getModuleMenu().parent();
            if (StringUtils.isBlank(itemKey)) {
                itemKey = menuTag.getModuleMenu().menuKey();
            }
            ResourceItem resourceItem = menuItemMap.get(itemKey);
            if (resourceItem == null) {
                log.warn("menu item not define in the application:{}, key:{}", libraExtProperties.getExtKey(), menuTag.getModuleMenu().menuKey());
                continue;
            }
            if (StringUtils.isNotBlank(menuTag.getModuleMenu().uri())) {
                resourceItem.addPageUri(menuTag.getModuleMenu().uri());
            }

            resourceItem.addMenuOperation(menuTag);
//            resourceItem.setPath(menuTag.getPath());
            //resourceItem.setAppKey(libraExtProperties.getExtKey());
            fillMenuItem(menuTag.getModuleMenu(), resourceItem);

            if (loadOptElement) {
                for (OptElement optElement : menuTag.getOptElements()) {
                    resourceItem.addOpt(optElement);
                }
            }
        }//end for menuTag
    }

    private static void fillMenuMap(Map<String, ResourceItem> menuItemMap, List<ResourceItem> resourceItems,String appKey) {

        for (ResourceItem resourceItem : resourceItems) {
            resourceItem.setAppKey(appKey);
            menuItemMap.put(resourceItem.getItemKey(), resourceItem);
            if (CollectionUtils.isNotEmpty(resourceItem.getItems())) {
                fillMenuMap(menuItemMap, resourceItem.getItems(),appKey);
            }
        }
    }

    private static void fillMenuItem(ModuleMenu moduleMenu, ResourceItem resourceItem) {
        if (resourceItem.getIcon() == null) {
            resourceItem.setIcon(moduleMenu.icon());
        }
        if (resourceItem.getName() == null) {
            resourceItem.setName(moduleMenu.name());
        }
        if (resourceItem.getUri() == null) {
            resourceItem.setUri(moduleMenu.uri());
        }
    }

}
