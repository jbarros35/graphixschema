package com.graphixschema.database.plugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

/**
 * @author jlopesde
 *
 */
public class VerifyInput implements VerifyListener {
	
	private boolean upperCase = false;
	private VerifyType type;
	
			
	public VerifyInput(VerifyType type) {
		super();
		this.type = type;
	}



	public VerifyInput(boolean upperCase, VerifyType type) {
		super();
		this.upperCase = upperCase;
		this.type = type;
	}



	@Override
	public void verifyText(VerifyEvent e) {
	   doIt(e);
	}

	private void doIt(VerifyEvent e) {
		switch (type) {
			case DIGIT: {
				e.doit = Character.isDigit(e.character)	            
					    || e.keyCode == SWT.ARROW_LEFT
					    || e.keyCode == SWT.ARROW_RIGHT
					    || e.keyCode == SWT.BS;
				break;
			}
			
			case LETTER: {
				e.doit = Character.isLetter(e.character)	            
					    || e.keyCode == SWT.ARROW_LEFT
					    || e.keyCode == SWT.ARROW_RIGHT
					    || e.keyCode == SWT.BS;
				break;
			}
		}
		
		if (upperCase) {
			e.text = e.text.toUpperCase();
		}
	}
	
}
