package com.graphixschema.database.plugin;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class DirectorySelect implements Runnable {

	String path;
	String message;
	String filterPath;
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {		
		Shell shell =  PlatformUI.getWorkbench().getDisplay().getActiveShell();
		 DirectoryDialog dialog = new  DirectoryDialog(shell);
		dialog.setText(message);
		dialog.setFilterPath(filterPath);
		path = dialog.open();
	}		
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public String getFilterPath() {
		return filterPath;
	}

	public void setFilterPath(String filterPath) {
		this.filterPath = filterPath;
	}

}
