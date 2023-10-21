package io.innospots.base.store;

/**
 * @author Smars
 * @date 2023/7/21
 */
public interface CacheStore {


    void save(String key,String value);

    String get(String key);

    boolean remove(String key);
}
