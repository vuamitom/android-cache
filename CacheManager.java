package com.knx.nation.common;



import java.io.*;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.knx.nation.NationNews;

/**
 * 
 * @author Tam
 * Description: lightweight cache manager for Android
 * TO-DO : check for valid key as a filename
 * 		   find a way to get Context for any application class
 */
public class CacheManager {
    public static final String TAG = "CacheManager"; //static for now
	public static final long DEFAULT_CACHE_DURATION = 604800000; // a week
    public static final long DEFAULT_BITMAP_CACHE_DURATION = 86400000; // a day
	public static final String CACHE_FILE = "knx_cache_file"; //static for now
	public static final String SPLITTER = "===";
	private static final int MAX_FILE_NAME_LENGTH = 120; // we will get a ENAMETOOLONG exception on a few phones without this
	private static CacheManager currentCache;
	private Context context;
	private File cacheDir;
	private HashMap<String, CacheRecord> cacheList;
	private CacheManager(Context context){
		//context = NationNews.getInstance().getApplicationContext();
		cacheDir = context.getCacheDir();
		Log.i("Cache Manager", cacheDir.getAbsolutePath());
		FileInputStream in  = null;
		cacheList = new HashMap<String, CacheRecord>();
		try {
			in = new FileInputStream(new File(cacheDir.getAbsolutePath() + File.separator + CACHE_FILE));
			
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null){
				String[] tokens = line.split(SPLITTER);
				//Date savedDate = new Date(Integer.parseInt(tokens[1]));
				if (Calendar.getInstance().getTime().getTime() - Long.parseLong(tokens[1]) < Long.parseLong(tokens[2])){
					CacheRecord cr = new CacheRecord(tokens[0], Long.parseLong(tokens[1]), Long.parseLong(tokens[2]));
					cacheList.put(tokens[0], cr);			
				}
				else{
					//remove expired cache file 
					File toRemove = new File(getCacheFileNameFromKey(tokens[0]));
                    Log.i(TAG, "CacheRemove " + toRemove.getAbsolutePath());
					if(toRemove.exists())
						toRemove.delete();
				}
			}
            Log.i(TAG, "Total number of cached items = " + cacheList.size());
			br.close();
			in.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.e("CacheManager", "Cache file doesn't exist");
		}
		//read
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch (Exception e){
            Log.e("CacheManager","",e) ;
        }
		
	}



    public static void initialize(Context context){
        currentCache = new CacheManager(context);
    }

	public static CacheManager getInstance()
	{
		if(currentCache == null){
			currentCache = new CacheManager(NationNews.getInstance().getApplicationContext());
		}
		return currentCache;
	}


    private String getCacheFileNameFromKey(String key){
        /*
        try {
            key= URLEncoder.encode(key, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            Log.e("CacheManager", e1.getMessage());
        }
        String filename = cacheDir.getAbsolutePath() + File.separator + key;
        Log.d(TAG, "Cachefilename " + filename);
        if(filename.length() > MAX_FILE_NAME_LENGTH ){
            key = key.substring(0, key.length() + (MAX_FILE_NAME_LENGTH - filename.length()) );
            filename = cacheDir.getAbsolutePath() + File.separator + key;
        }
        */
        String hash = String.valueOf(key.hashCode());
//        if(hash.indexOf("-")>=0){
//            hash = hash.replaceFirst("-","1");
//        }
        String filename = cacheDir.getAbsolutePath() + File.separator + hash;
        return filename;
    }

	public String get(String key){
		
		if(cacheList.containsKey(key)){
			FileInputStream in = null;
			try {
				in = new FileInputStream(new File(getCacheFileNameFromKey(key)));
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String temp;
				StringBuffer result = new StringBuffer();
				while ((temp = br.readLine())!=null){
					result.append(temp);
				}
				Log.i("Cache Manger","Cache hit for key " + key);
				br.close();
				in.close();
				if(key.equals("SECTION_LIST")) Log.i("Cache", "Section_list " + result.toString()); 
				return result.toString();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e("CacheManager"," ", e);
				//if file not found
				Log.w("Cache Manager", "cache file alr removed");
				synchronized(cacheList){
					cacheList.remove(key);
				}
				e.printStackTrace();
				updateMasterCache();				
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("CacheManager"," ", e);

				return null;
			} catch (Exception e){
				Log.e("CacheManager"," ", e);

				return null;
			}
			
		}
		else{
			return null;
		}
			
	}
	public void set(String key, String content){
		set(key, content, DEFAULT_CACHE_DURATION);
	}
	
	/**
	 * 
	 * @param key 
	 * @param content
	 * @param duration
	 */
	public void set(String key, String content, Long duration){		
		if(key.equals(CACHE_FILE))
			return;
		try {

			FileOutputStream os = new FileOutputStream(new File(getCacheFileNameFromKey(key)));
			os.write(content.getBytes());
			os.close();
			synchronized(cacheList){
				cacheList.put(key, new CacheRecord(key, new Date().getTime(), duration));
			}
			//update master cache file
			updateMasterCache();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e("CacheManager", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("CacheManager", e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			Log.e("CacheManager", e.getMessage());
			e.printStackTrace();
		}
				
	}

    public InputStream getStream(String key){
        if(cacheList.containsKey(key)){
            try {
                FileInputStream ins = new FileInputStream(new File(getCacheFileNameFromKey(key)));
                return ins;
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return null;
        }
        else
            return null;
    }

    public Bitmap getBitmap(String key){

        try{
            if(cacheList.containsKey(key)){

                Log.d(TAG, "getBitmap bitmap exists for key " + key);
                InputStream ins = getStream(key);
                if (ins != null){
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    //o.inJustDecodeBounds = true;
                    return BitmapFactory.decodeStream(ins);
                }
            }
        }catch (OutOfMemoryError error){
            MemCacheManager.getInstance().reset();
            Log.e(TAG, "CacheManger -> getBitmap", error);
        }
        return null;
    }

    public void setBitmap(String key, Bitmap bm){
        setBitmap(key, bm, DEFAULT_BITMAP_CACHE_DURATION);
    }

    public void setBitmap(String key, Bitmap bm, Long duration){
        try {

            String filename = getCacheFileNameFromKey(key);
            Log.d(TAG, "Cachefilename SAVE " + filename + " \n KEY = " + key);
            FileOutputStream os = new FileOutputStream(new File(filename));
            bm.compress(Bitmap.CompressFormat.JPEG, 50, os);
            os.flush();
            os.close();
            synchronized(cacheList){
                cacheList.put(key, new CacheRecord(key, new Date().getTime(), duration));
            }
            //update master cache file
            updateMasterCache();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void setStream(String key, InputStream ins){
        setStream(key, ins, DEFAULT_CACHE_DURATION);
    }

    public void setStream(String key, InputStream ins, Long duration){


        try {
            FileOutputStream os = new FileOutputStream(new File(getCacheFileNameFromKey(key)));
            final int buffer_size=1024;

            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=ins.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
            os.close();
            synchronized(cacheList){
                cacheList.put(key, new CacheRecord(key, new Date().getTime(), duration));
            }
            //update master cache file
            updateMasterCache();

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

	private void updateMasterCache()
	{
		try {
			String cacheFilePath = cacheDir.getAbsolutePath() + File.separator + CACHE_FILE;
			//context.deleteFile(cacheFilePath);
			FileOutputStream  os = new FileOutputStream(new File(cacheFilePath));
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(os));
			synchronized(cacheList){
				for( String key : cacheList.keySet()){
					br.write(cacheList.get(key).toString() + "\n");
				}
			}
			br.close();
			os.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static class CacheRecord{
		String key;
		Long startDate, duration;
		public CacheRecord(String key, Long startDate, Long duration){
			this.key = key;
			this.startDate = startDate;
			this.duration = duration;
		}
		
		public String toString(){
			return key + CacheManager.SPLITTER + startDate + CacheManager.SPLITTER + duration;
		}
	}



}
