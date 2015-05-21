package com.graphixschema.database.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.ResourceManager;



import com.graphixschema.database.plugin.AbstractView;
import com.graphixschema.database.plugin.FileSelect;
import com.graphixschema.database.vo.ConnectionProperties;
import com.mxgraph.model.mxCell;

import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

/**
 * 
 * @author jlopesde
 *
 */
public class OldGraphApp extends ApplicationWindow {
	
	private Text txtSearch;
	private Composite mapComposite;
	private GraphViewer viewer;
	private Button btnSearch;

	private DBaseRuleLoader generator;
	private Button btnSave;
	private Button btnSQL;
	private File prologFile;
	private Label lblQtd;

	/**
	 * Create the application window.
	 */
	public OldGraphApp() {
		super(null);
		setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		GridData gd_container = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1);
		container.setLayoutData(gd_container);

		Composite searchComposite = new Composite(container, SWT.NONE);
		searchComposite.setLayout(new GridLayout(10, false));
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));

		txtSearch = new Text(searchComposite, SWT.BORDER);
		txtSearch.setEnabled(false);
		GridData gd_txtSearch = new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1);
		gd_txtSearch.widthHint = 402;
		txtSearch.setLayoutData(gd_txtSearch);

		btnSearch = new Button(searchComposite, SWT.NONE);
		btnSearch.setEnabled(false);
		btnSearch.addSelectionListener(new SelectionAdapter() {
			/**
			 * Search a table in graph.
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.searchCell(txtSearch.getText().toUpperCase());
			}
		});
		btnSearch.setText("Buscar Tabla");

		Button btnNew = new Button(searchComposite, SWT.NONE);
		btnNew.setToolTipText("Crear Nuevo Mapa del banco de datos.");
		btnNew.setImage(SWTResourceManager.getImage(GraphApp.class, "/new_con.gif"));
		btnNew.addSelectionListener(new SelectionAdapter() {
			// create a new map from database.
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadTables();
			}
		});
		// Button load map
		Button btnLoad = new Button(searchComposite, SWT.NONE);
		btnLoad.addSelectionListener(new SelectionAdapter() {

			/**
			 * Load map file.
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					FileSelect fileDir = new FileSelect(SWT.OPEN,
							new String[] { "*.pl;" });
					fileDir.setMessage("Seleccione el archivo de mapa selecionado *.pl.");
					fileDir.setFilterPath("C:\\");
					Display.getDefault().syncExec(fileDir);
					final String filePath = fileDir.getPath();
					if (filePath == null) {
						return;
					}
				
					loadGraph(new File(filePath));
					
					prologFile = new File(filePath);
					// try to find the serial file at same path
					generator = new DBaseRuleLoader();
					String sessFileName = prologFile.getName().replace(".pl",
							".ser");
					File sessionFile = new File(prologFile.getParent() + "/"
							+ sessFileName);

					if (sessionFile.exists()) {
						// set the session
						generator.loadSession(sessionFile);
						// set prolog map
						generator.setFile(prologFile);
						enableButtons(true);
						// MessageDialog.openInformation(Display.getDefault().getActiveShell(),
						// "Informacion",
						// "Mapa cargado exitosamente.");
					} else {
						enableButtons(false);
						MessageDialog
								.openInformation(
										Display.getDefault().getActiveShell(),
										"Informacion",
										"Archivo de session no encontrado ("
												+ sessionFile.getAbsolutePath()
												+ "), no se puede generar scripts sin el archivo .ser en la misma ruta del mapa.");
					}
					enableButtons(true);
				} catch (Exception excpt) {
					excpt.printStackTrace();
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Error",
							"Error al cargar el mapa \n" + excpt);
				}
			}

		});
		btnLoad.setToolTipText("Cargar un mapa ya listo. Archivo .pl.");
		btnLoad.setImage(SWTResourceManager.getImage(GraphApp.class, "/import_wiz.gif"));
		// Button generate SQL
		btnSQL = new Button(searchComposite, SWT.NONE);
		btnSQL.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				generateSQL();
			}
		});
		btnSQL.setEnabled(false);
		btnSQL.setImage(SWTResourceManager.getImage(GraphApp.class, "/sql_query.png"));

		btnSave = new Button(searchComposite, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveMap();
			}
		});
		btnSave.setEnabled(false);
		btnSave.setToolTipText("Guardar mapa");
		btnSave.setImage(SWTResourceManager.getImage(GraphApp.class, "/save_edit.gif"));
		new Label(searchComposite, SWT.NONE);
		
		Label lblQtdReg = new Label(searchComposite, SWT.NONE);
		lblQtdReg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblQtdReg.setText("Qtd reg.");

		final Scale scale = new Scale(searchComposite, SWT.NONE);
		scale.setPageIncrement(100);
		GridData gd_scale = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_scale.widthHint = 890;
		scale.setLayoutData(gd_scale);
		scale.setMaximum(10000);
		lblQtd = new Label(searchComposite, SWT.NONE);
		lblQtd.setAlignment(SWT.CENTER);
		GridData gd_lblQtd = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblQtd.widthHint = 67;
		lblQtd.setLayoutData(gd_lblQtd);
		lblQtd.setText("1");
		 scale.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event evt) {
				lblQtd.setText(""+scale.getSelection());
			}
		});
	
		mapComposite = new Composite(container, SWT.NO_BACKGROUND
				| SWT.EMBEDDED);
		mapComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		getShell().setMaximized(true);
		return container;
	}

	/**
	 * 
	 */
	void generateSQL() {
		// check if there is a selected source
		Set<mxCell> target = viewer.getTarget();
		if (target != null && !target.isEmpty()) {
			// open a connection to Database.
			
			Set<String> dependencies = viewer
					.resolveDependencies();
			Integer qtd = Integer.parseInt(lblQtd.getText());
			// if it only one open a window normally as a plugin
			if (qtd == 1) {
				SQLDialog sqlDialog = new SQLDialog(getShell());
				sqlDialog.setDependenciesList(dependencies);
				sqlDialog.setGenerator(generator);
				int option = sqlDialog.open();
				if (option == 0) {
					final Clipboard cb = new Clipboard(Display.getCurrent());
					String textData = sqlDialog.getSQL();
					TextTransfer textTransfer = TextTransfer.getInstance();
					cb.setContents(new Object[] { textData },
							new Transfer[] { textTransfer });
				}
			} else {
				// open dialog for connection setings
				ConnDialog conn = new ConnDialog(getShell());
				if (conn.open() == 0) {
					final ConnectionProperties connProps = conn
							.getConnectionProperties();
					// case quantity is greater than one create a new file.
					boolean confirmation = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
							"Información", "Iniciando la generación de datos en el BBDD desea proseguir?");
					if (confirmation) {
						// disable all buttons
						enableButtons(false);
						BatchGenerator sqlGen = new BatchGenerator(generator);
						sqlGen.setConnectionProps(connProps);
						sqlGen.setDependencies(dependencies);
						sqlGen.setRecordQuantity(qtd);
						try {
							sqlGen.startBatch();
							MessageDialog.openInformation(Display.getDefault().getActiveShell(),
									"Información", "Ejecución Batch terminada, revise las tablas del BBDD.");
						} catch (Exception e) {
							e.printStackTrace();
							MessageDialog.openError(Display.getDefault().getActiveShell(),
									"Error", "Error al ejecutar los scripts batch!\n"+e.getMessage());
						}
						//enable after finished process.
						enableButtons(true);
					}
				}
				
			}
		} else {
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Validación", "Elija una tabla para generar los SQL.");
		}
	}

	/**
	 * 
	 */
	private void enableButtons(boolean enable) {
		txtSearch.setEnabled(enable);
		btnSearch.setEnabled(enable);
		btnSave.setEnabled(enable);
		btnSQL.setEnabled(enable);
	}

	/**
	 * 
	 */
	private void loadGraph(File mapFile) {
		java.awt.Frame frame = SWT_AWT.new_Frame(mapComposite);
		frame.setLocation(0, 0);
		frame.setBounds(0, 0, 800, 600);
		frame.setLayout(new BorderLayout());
		frame.setBackground(Color.WHITE);
		forceFocus(frame);
		viewer = new GraphViewer();
		viewer.setPrologMap(mapFile);
		viewer.viewGraph();
		frame.add(viewer);
	}

	/**
	 * Load from database a new model and read it. NOTE: Its not saved yet.
	 */
	private void loadTables() {
		boolean continuar = false;
		continuar = MessageDialog
				.openConfirm(
						Display.getDefault().getActiveShell(),
						"Confirmacion",
						"Esta operación toma algunos minutos para mapear la estructura de base de datos completa, puede guardar el progreso después si lo desea en un archivo de mapa. "
								+ "\n ¿De verdad desea proceder?");
		if (continuar) {
			// open dialog for connection setings
			ConnDialog conn = new ConnDialog(getShell());
			if (conn.open() == 0) {
				final ConnectionProperties connProps = conn
						.getConnectionProperties();	
		
								generator = new DBaseRuleLoader();
								generator.connect(connProps.getUrl(),
										connProps.getUser(),
										connProps.getPassword());
								// load for all maps.
								generator.generatePrologTree();
								// load visualization graph
								loadGraph((File) generator.getFile());
						
				MessageDialog.openInformation(
						Display.getDefault().getActiveShell(), "Informacion",
						"Tablas cargadas con sucesso.");
				enableButtons(true);
			}
		}
	}

	/**
	 * Save the file and serialize the array of objects.
	 */
	private void saveMap() {
		FileSelect fileDir = new FileSelect(SWT.SAVE, new String[] { "*.pl;" });
		fileDir.setMessage("Guardar el archivo de mapa de la base de datos.");
		fileDir.setFilterPath("C:\\");
		Display.getDefault().syncExec(fileDir);
		String filePath = fileDir.getPath();
		if (filePath == null) {
			return;
		}
		try {
			generator.saveSession(filePath);
			MessageDialog.openInformation(
					Display.getDefault().getActiveShell(), "Informacion",
					"Mapa guardado exitosamente en " + filePath);
		} catch (Exception ex) {
			ex.printStackTrace();
			AbstractView.showError(ex);
		}
	}

	/**
	 * 
	 * @param frameAwt
	 */
	private void forceFocus(final Frame frameAwt) {
		// (A) force focus when AWT childs receive mouse events
		java.awt.event.MouseListener mlAwt = new java.awt.event.MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// System.err.println("force focus from AWT entered \t\t"+e);
				mapComposite.getDisplay().asyncExec(new Runnable() { // (shift
																		// to
																		// SWT
																		// thread)
							@Override
							public void run() {
								mapComposite.forceFocus();
							}
						});
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// System.err.println("force focus from AWT entered \t\t"+e);
				mapComposite.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						mapComposite.forceFocus();
					}
				});
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		};
		java.awt.event.MouseMotionListener mlAwt2 = new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// System.err.println("force focus from AWT entered \t\t"+e);
				mapComposite.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						mapComposite.forceFocus();
					}
				});
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		};
		// (B) forward messages from SWT to AWT
		mapComposite
				.addMouseWheelListener(new org.eclipse.swt.events.MouseWheelListener() {

					@Override
					public void mouseScrolled(
							final org.eclipse.swt.events.MouseEvent event) {
						System.out.println(event);
						// (shift to AWT thread)
						EventQueue.invokeLater(new Runnable() {

							public void run() {

								Component c = SwingUtilities
										.getDeepestComponentAt(frameAwt,
												event.x, event.y);
								if (c != null) {
									java.awt.Point bp = frameAwt
											.getLocationOnScreen();
									java.awt.Point cp = c.getLocationOnScreen();
									MouseEvent e = new MouseWheelEvent(
											c,
											MouseEvent.MOUSE_WHEEL,
											event.time & 0xFFFFFFFFL,
											0, // modifiers
											event.x - (cp.x - bp.x),
											event.y - (cp.y - bp.y),
											0, // click count
											false,
											MouseWheelEvent.WHEEL_UNIT_SCROLL,
											-event.count, -event.count);
									// System.out.println("dispatching AWT event "+e);
									c.dispatchEvent(e);

								}
							}
						});
					}
				});

		// <childrenAwtComponent>.addMouseListener(mlAwt);
		// <childrenAwtComponent>.addMouseMotionListener(mlAwt2);
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the status line manager.
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Configure the shell.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Mapa de Datos Mercadona");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1735, 841);
	}
}
