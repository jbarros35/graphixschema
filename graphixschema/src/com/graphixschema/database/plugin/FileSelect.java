package com.graphixschema.database.plugin;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class FileSelect implements Runnable {

	String path;
	String message;
	String filterPath;
	int style;
	String[] extensions;
	
	
	public FileSelect(int style, String[] extensions) {
		super();
		this.style = style;
		this.extensions = extensions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {		
		Shell shell =  Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(shell, style);		
		dialog.setFilterExtensions(extensions);
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
