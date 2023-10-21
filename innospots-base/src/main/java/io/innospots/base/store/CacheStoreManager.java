package io.innospots.base.store;

/**
 * @author Smars
 * @date 2023/7/21
 */
public class CacheStoreManager {

    private static CacheStore cacheStore;

    public static void set(CacheStore cacheStore){
        CacheStoreManager.cacheStore = cacheStore;
    }

    public static CacheStore getCache(){
        return cacheStore;
    }

    public static void save(String key,String value){
        if(cacheStore!=null){
            cacheStore.save(key,value);
        }
    }

    public static String get(String key){
        if(cacheStore!=null){
            return cacheStore.get(key);
        }
        return null;
    }

    public static boolean remove(String key){
        if(cacheStore!= null){
            return cacheStore.remove(key);
        }
        return false;
    }



}
