package com.graphixschema.database.graph;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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

public class ConnDialog extends Dialog {

	private static final String URL = "jdbc:oracle:thin:@<DBSERVER>:<PORT>:<SID>";
	Combo txtUrl;
	Text txtUser;
	Text txtPass;
	Text txtSchema;
	Set<String> urlList;
	String connectionUrl;
	String connectionUser;
	String connectionPassword;
	String connectionSchema;
	Button testButton;

	private ConnectionProperties connectionProperties;
	private Composite container;
	private Group group;
	private Button btnSave;
	private boolean success = false;

	public ConnDialog(Shell parent) {
		super(parent);
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configurar conección");
	}

	/**
	 * 
	 */
	private void saveCache() {
		try {
			OracleConnection connection = new OracleConnection();
			connectionProperties.setUrl(txtUrl.getText());
			connectionProperties.setUser(txtUser.getText());
			connectionProperties.setPassword(txtPass.getText());
			connectionProperties.setSchema(txtSchema.getText());
			connection.testConnection(connectionProperties);
			showInfo("Conexión guardada con éxito.");
			Cache.putCache(Cache.Type.CONNECTIONPROPERTIES,
					connectionProperties);
		} catch (SQLException e) {
			showError("No fue posible conectarse a la base de datos:"
					+ e.getMessage());
		} catch (Exception e) {
			showError("Error no esperado:" + e.getMessage());
		}
	}

	/**
	 * 
	 */
	private void loadCache() {
		connectionProperties = (ConnectionProperties) Cache
				.getCache(Cache.Type.CONNECTIONPROPERTIES);
		if (connectionProperties != null) {
			txtUrl.add(connectionProperties.getUrl());
			txtUrl.select(0);
			txtUser.setText(connectionProperties.getUser());
			txtPass.setText(connectionProperties.getPassword());
			txtSchema.setText(connectionProperties.getSchema());
		} else {
			connectionProperties = new ConnectionProperties();
			urlList = new HashSet<String>();
			txtUrl.setText(URL);
		}

	}

	/**
	 * 
	 */
	public boolean testConnection() {
		try {
			OracleConnection connection = new OracleConnection();
			connectionProperties.setUrl(txtUrl.getText());
			connectionProperties.setUser(txtUser.getText());
			connectionProperties.setPassword(txtPass.getText());
			connectionProperties.setSchema(txtSchema.getText().toUpperCase());
			connection.testConnection(connectionProperties);
			success = true;
			showInfo("Conexión establecida con éxito.");
		} catch (SQLException e) {
			showError("No fue posible conectarse a la base de datos:"
					+ e.getMessage());
		} catch (Exception e) {
			showError("Error no esperado:" + e.getMessage());
		}
		return success;
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
		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		group = new Group(container, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		GridData gd_group = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,
				5);
		gd_group.heightHint = 208;
		gd_group.widthHint = 556;
		group.setLayoutData(gd_group);
		// URL
		Label lblUrl = new Label(group, SWT.NONE);
		lblUrl.setText("Url:");

		txtUrl = new Combo(group, SWT.None);
		GridData gd_txtUrl = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_txtUrl.widthHint = 375;
		txtUrl.setLayoutData(gd_txtUrl);

		// GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		// txtUrl.setLayoutData(gd);

		// User
		Label lblUser = new Label(group, SWT.NONE);
		lblUser.setText("Usuario:");

		txtUser = new Text(group, SWT.BORDER | SWT.SINGLE);
		GridData gd_txtUser = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_txtUser.widthHint = 309;
		txtUser.setLayoutData(gd_txtUser);

		// txtUser.setLayoutData(gd);
		// password
		Label lblPass = new Label(group, SWT.NONE);
		lblPass.setText("Contraseña:");

		txtPass = new Text(group, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		GridData gd_txtPass = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_txtPass.widthHint = 309;
		txtPass.setLayoutData(gd_txtPass);
		txtPass.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// validate
				if (isTextNonEmpty(txtPass)) {
					connectionProperties.setPassword(txtPass.getText());
				}
			}

		});

		// schema
		Label lblSchema = new Label(group, SWT.NONE);
		lblSchema.setText("Schema:");

		txtSchema = new Text(group, SWT.BORDER | SWT.SINGLE);
		GridData gd_txtSchema = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_txtSchema.widthHint = 309;
		txtSchema.setLayoutData(gd_txtSchema);

		testButton = new Button(group, SWT.PUSH);
		testButton.setText("Probar la conexión");
		// testButton.setEnabled(false);
		testButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				testConnection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		btnSave = new Button(group, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveCache();
			}
		});
		btnSave.setText("Guardar conexi\u00F3n");
		txtSchema.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// validate
				if (isTextNonEmpty(txtSchema)) {
					connectionProperties.setSchema(txtSchema.getText());
				}
			}

		});
		txtUser.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				String value = txtUser.getText();
				txtSchema.setText(value.toUpperCase());
				txtPass.setText(value.toUpperCase());
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		txtUser.addKeyListener(new KeyListener() {
						
			// updates schema value.
			@Override
			public void keyPressed(KeyEvent e) {
				
				//e.keyCode == 127
				if (e.keyCode >=48 && e.keyCode <=122) {
					String value = txtSchema.getText() + e.character;
					txtSchema.setText(value.toUpperCase());
				} else {
					txtSchema.setText("");
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// validate
				if (isTextNonEmpty(txtUser)) {
					connectionProperties.setUser(txtUser.getText());
				}
				
			}

		});

		txtUrl.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// connectionProperties.setUrl(txtUrl.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// connectionProperties.setUrl(txtUrl.getText());
			}
		});
		txtUrl.addVerifyListener(new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				String text = txtUrl.getText();
				String newText = text.substring(0, e.start) + e.text
						+ text.substring(e.end);
			}
		});

		txtUrl.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == 16) {
					e.doit = false;
					e.detail = SWT.TRAVERSE_NONE;
					String newText = txtUrl.getText();
					try {
						// connectionProperties.setUrl(txtUrl.getText());
						urlList.add(newText);
						txtUrl.add(newText);
						txtUrl.setSelection(new Point(0, newText.length()));
					} catch (NumberFormatException ex) {
					}
				}
			}
		});
		loadCache();
		return container;
	}

	public ConnectionProperties getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * @param txt
	 * @return
	 */
	private boolean isTextNonEmpty(Text txt) {
		return !txt.getText().isEmpty();
	}
}
