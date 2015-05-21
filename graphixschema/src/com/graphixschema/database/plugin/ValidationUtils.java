package com.graphixschema.database.plugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class ValidationUtils {
	
	public static Map<Character, Object> ONLYLETTERS = new HashMap<Character, Object>();
	
	static {
		ONLYLETTERS.put('A', true); 
		ONLYLETTERS.put('B', true); 
		ONLYLETTERS.put('C', true); 
		ONLYLETTERS.put('D', true); 
		ONLYLETTERS.put('E', true); 
		ONLYLETTERS.put('F', true);
		ONLYLETTERS.put('G', true);
		ONLYLETTERS.put('H', true);
		ONLYLETTERS.put('I', true);
		ONLYLETTERS.put('J', true); 
		ONLYLETTERS.put('K', true); 
		ONLYLETTERS.put('L', true);
		ONLYLETTERS.put('M', true); 
		ONLYLETTERS.put('N', true); 
		ONLYLETTERS.put('O', true); 
		ONLYLETTERS.put('P', true); 
		ONLYLETTERS.put('Q', true); 
		ONLYLETTERS.put('R', true);
		ONLYLETTERS.put('S', true); 
		ONLYLETTERS.put('T', true); 
		ONLYLETTERS.put('U', true); 
		ONLYLETTERS.put('V', true); 
		ONLYLETTERS.put('X', true); 
		ONLYLETTERS.put('Z', true);
		ONLYLETTERS.put('a', true); 
		ONLYLETTERS.put('b', true); 
		ONLYLETTERS.put('c', true); 
		ONLYLETTERS.put('d', true); 
		ONLYLETTERS.put('e', true); 
		ONLYLETTERS.put('f', true);
		ONLYLETTERS.put('g', true); 
		ONLYLETTERS.put('h', true); 
		ONLYLETTERS.put('i', true); 
		ONLYLETTERS.put('j', true); 
		ONLYLETTERS.put('k', true);
		ONLYLETTERS.put('l', true);
		ONLYLETTERS.put('m', true); 
		ONLYLETTERS.put('n', true); 
		ONLYLETTERS.put('o', true); 
		ONLYLETTERS.put('p', true); 
		ONLYLETTERS.put('q', true); 
		ONLYLETTERS.put('r', true);
		ONLYLETTERS.put('s', true); 
		ONLYLETTERS.put('t', true); 
		ONLYLETTERS.put('u', true); 
		ONLYLETTERS.put('v', true); 
		ONLYLETTERS.put('x', true); 
		ONLYLETTERS.put('z', true);
	}
	
	/**
	 * @param txt
	 * @return
	 */
	public static boolean isTextNonEmpty(Text txt) {
		return !txt.getText().isEmpty();
	}
	
	/**
	 * @param txt
	 * @return
	 */
	public static boolean isTextNonEmpty(Combo cmb) {
		return !cmb.getText().isEmpty();
	}
	
	/**
	 * @param list
	 * @return
	 */
	public static boolean isListNotEmpty(org.eclipse.swt.widgets.List list) {
		boolean empty = true;
		if (list != null) {
			empty = list.getItemCount() == 0;
		}
		return empty;
	}

}
