package com.graphixschema.database.graph;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.graphixschema.database.OracleConnection;
import com.graphixschema.database.plugin.DisplayMsg;
import com.graphixschema.database.utilities.Cache;
import com.graphixschema.database.vo.ConnectionProperties;

import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FillLayout;

public class SQLDialog extends Dialog {

	private Set<String> dependenciesList;
	private Text txtSQL;
	
	private String sql;
	// needed for SQL generation
	private DBaseRuleLoader generator;
	
	public SQLDialog(Shell parent) {
		super(parent);
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Generar SQL");
	}

	/**
	 * @param e
	 */
	private void showError(String message) {
		DisplayMsg msg = new DisplayMsg(MessageDialog.ERROR, "Error", message);
		Display.getDefault().syncExec(msg);
	}

	/**
	 * @param e
	 */
	private void showInfo(String message) {
		DisplayMsg msg = new DisplayMsg(MessageDialog.INFORMATION, "Info",
				message);
		Display.getDefault().syncExec(msg);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		
		txtSQL = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_txtSQL = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtSQL.widthHint = 1104;
		gd_txtSQL.heightHint = 425;
		txtSQL.setLayoutData(gd_txtSQL);
		// start generation of sql
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		GraphSQLGenerator sqlGen = new GraphSQLGenerator(generator);
		sql = sqlGen.generateSQL(dependenciesList);
		txtSQL.setText(sql);
		return container;
	}

	public Set<String> getDependenciesList() {
		return dependenciesList;
	}

	public void setDependenciesList(Set<String> dependenciesList) {
		this.dependenciesList = dependenciesList;
	}

	public DBaseRuleLoader getGenerator() {
		return generator;
	}

	public void setGenerator(DBaseRuleLoader generator) {
		this.generator = generator;
	}

	public String getSQL() {
		return sql;
	}
	
	
}
