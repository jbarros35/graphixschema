package com.graphixschema.database.plugin;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


public class AbstractView {

	public static int showConfirm(String message, Shell shell) {
		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION
				| SWT.OK | SWT.CANCEL);
		dialog.setText("Confirmar operación");
		dialog.setMessage(message);
		return dialog.open();
	}
	
	/**
	 * @param e
	 */
	public static void showError(Exception e) {
		DisplayMsg msg = new DisplayMsg(MessageDialog.ERROR,"Error", "Hubo un error inesperado:"+e.getMessage());
		Display.getDefault().syncExec(msg);		
	}

	/**
	 * @param e
	 */
	public static void showInfo(String message) {
		DisplayMsg msg = new DisplayMsg(MessageDialog.INFORMATION,"Informacion", message);
		Display.getDefault().syncExec(msg);		
	}
	
	/**
	 * @return
	 */
	public static Shell getShell() {
		return PlatformUI.getWorkbench().getDisplay().getActiveShell();
	}
	
}
