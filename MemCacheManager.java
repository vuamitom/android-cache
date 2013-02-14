package com.knx.nation.common;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Tam
 * Date: 17/12/12
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class MemCacheManager {
    public static final String TAG = "MemCacheManager";
    public static final int MAX_SIZE = 20;
//    private Map<String, Bitmap> bitmapCache;
//    private LinkedList<String> priorityQueue;
    private LruCache bitmapCache;

    public static MemCacheManager instance;


    private MemCacheManager(){
        //bitmapCache = new HashMap<String, Bitmap>();
        bitmapCache = new LruCache<String, Bitmap>(MAX_SIZE){
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue){
                if(oldValue != null && !oldValue.isRecycled())
                    oldValue.recycle();
            }
        };
        //priorityQueue = new LinkedList<String>();

    }


    public static MemCacheManager getInstance(){
        if(instance == null)
            instance = new MemCacheManager();
        return instance;
    }


    public void set(String key, Bitmap bm){
        synchronized (bitmapCache){
//            if(bitmapCache.size() > MAX_SIZE){
//                //remove
//                String url = priorityQueue.removeFirst();
//                if(url!=null){
//                    //remove equivalent in cache
//                    Bitmap oldBm = bitmapCache.remove(url);
//
//                    if(oldBm!=null)
//                        oldBm.recycle();
//                }
//            }
//            bitmapCache.put(key, bm);
//            priorityQueue.addLast(key);
            if(bitmapCache.get(key) == null)
                bitmapCache.put(key, bm);
        }
    }

    public void reset(){
//        synchronized (bitmapCache){
//            for(String url  : bitmapCache.keySet()){
//                bitmapCache.get(url).recycle();
//            }
//            bitmapCache = new  HashMap<String, Bitmap>();
//            priorityQueue = new LinkedList<String>();
//        }
        bitmapCache.evictAll();
    }


    public Bitmap get(String key){
        return (Bitmap)bitmapCache.get(key);
    }

}
