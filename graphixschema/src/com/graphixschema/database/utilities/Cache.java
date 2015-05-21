package com.graphixschema.database.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;

public abstract class Cache {

	/**
	 * Contains all values in cache inside.
	 */
	private static Map<String, Object> cacheMap = new HashMap<String, Object>();
	private static File fileCache = null;
	
	/**
	 * Cache valid types.
	 * @author jlopesde
	 *
	 */
	public enum Type {
		
		CONNECTIONPROPERTIES("connectionProperties"), SESSION("session");
		
		String type;
		
		Type(String stype) {
			this.type = stype;
		}
		
		public static Type fromString(String text) {
		    if (text != null) {
		      for (Type b : Type.values()) {
		        if (text.equalsIgnoreCase(b.type)) {
		          return b;
		        }
		      }
		    }
		    return null;
		  }
	}

	static {
		try {
			loadCache();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load a cache if exists if not create a new one at user location.
	 * @throws Exception
	 */
	protected static void loadCache() throws Exception {
		String userHome = System.getProperty("user.home");
		fileCache = new File((userHome + "/dataBase.cache").replace("file:/",
					""));
		if (fileCache.exists()) {
			readCache();
		} else {
			new File(userHome).mkdirs();
			fileCache.createNewFile();
			saveCache();
		}
		
	}

	/**
	 * @param f
	 * @throws IOException
	 */
	private static void saveCache() throws Exception {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		fos = new FileOutputStream(fileCache);
		out = new ObjectOutputStream(fos);
		out.writeObject(cacheMap);
		out.close();		
	}

	/**
	 * @param f
	 * @throws IOException
	 */
	private static void readCache() throws IOException {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(fileCache);
			in = new ObjectInputStream(fis);
			cacheMap = (Map) in.readObject();
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (Exception ex) {
			}
		}
	
	}

	public static Object getCache(Type key) {
		return cacheMap.get(key.type);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public static void putCache(Type key, Object value) throws Exception {
		cacheMap.put(key.type, value);
		saveCache();		
	}

	/**
	 * @param stringValue
	 * @return
	 */
	private static String buildStringFromArray(List<String> values) {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> it = values.iterator();
		while (it.hasNext()) {
			buffer.append(it.next());
			if (it.hasNext()) {
				buffer.append(",");
			}
		}

		return buffer.toString();
	}

	/**
	 * @param stringValue
	 * @return
	 */
	private static List<String> buildArrayFromString(String stringValue) {
		List<String> values = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(stringValue, ",");
		while (tokenizer.hasMoreElements()) {
			values.add((String) tokenizer.nextElement());
		}
		return values;
	}
}
