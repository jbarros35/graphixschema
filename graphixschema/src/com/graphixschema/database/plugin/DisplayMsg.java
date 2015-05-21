package com.graphixschema.database.plugin;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class DisplayMsg implements Runnable {
	
	String msg;
	String title;
	private int type;
	
	public DisplayMsg(int type, String title, String msg) {
		this.msg = msg;
		this.title = title;
		this.type = type;
	}
	
	public void run() {
		Shell shell =  Display.getDefault().getActiveShell();
		MessageDialog dialog = new MessageDialog(shell, title, null,
				msg,
				type,
			    //MessageDialog.ERROR,
			    new String[] { "Ok" }, 0);
		dialog.open();	
	}
	
}
