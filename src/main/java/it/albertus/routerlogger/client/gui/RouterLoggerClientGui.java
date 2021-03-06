package it.albertus.routerlogger.client.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.DisplayThreadExecutor;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.console.StyledTextConsole;
import it.albertus.routerlogger.client.engine.AppStatus;
import it.albertus.routerlogger.client.engine.Protocol;
import it.albertus.routerlogger.client.engine.RouterLoggerClientConfig;
import it.albertus.routerlogger.client.engine.Status;
import it.albertus.routerlogger.client.gui.listener.CloseListener;
import it.albertus.routerlogger.client.gui.listener.PreferencesListener;
import it.albertus.routerlogger.client.http.HttpPollingThread;
import it.albertus.routerlogger.client.mqtt.MqttClient;
import it.albertus.routerlogger.client.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.InitializationException;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class RouterLoggerClientGui extends ApplicationWindow {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerClientGui.class);

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	private final Configuration configuration = RouterLoggerClientConfig.getInstance();

	private final MqttClient mqttClient = MqttClient.getInstance();

	private final ThresholdsManager thresholdsManager;

	private TrayIcon trayIcon;
	private MenuBar menuBar;
	private SashForm sashForm;
	private DataTable dataTable;
	private StyledTextConsole console;

	private AppStatus currentStatus;
	private AppStatus previousStatus;

	private Thread mqttConnectionThread;
	private Thread httpPollingThread;

	public RouterLoggerClientGui() {
		super(null);
		thresholdsManager = new ThresholdsManager(this);
	}

	public static class Defaults {
		public static final boolean GUI_START_MINIMIZED = false;
		public static final boolean CONSOLE_SHOW_CONFIGURATION = false;
		public static final int MQTT_CONNECT_RETRY_INTERVAL_SECS = 5;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static void run(final InitializationException ie) {
		Display.setAppName(Messages.get("msg.application.name"));
		Display.setAppVersion(Version.getInstance().getNumber());
		final Display display = Display.getDefault();

		if (ie != null) { // Display error dialog and exit.
			EnhancedErrorDialog.openError(null, Messages.get("lbl.window.title"), ie.getLocalizedMessage() != null ? ie.getLocalizedMessage() : ie.getMessage(), IStatus.ERROR, ie.getCause() != null ? ie.getCause() : ie, Images.getMainIcons());
		}
		else {
			final RouterLoggerClientGui gui = new RouterLoggerClientGui();
			gui.open();
			final Shell shell = gui.getShell();
			try {
				Protocol.valueOf(gui.configuration.getString("client.protocol"));
				gui.connect();
			}
			catch (final RuntimeException e) {
				logger.log(Level.FINE, e.toString(), e);
				new PreferencesListener(gui).widgetSelected(null);
			}
			while (!shell.isDisposed()) {
				if (!display.isDisposed() && !display.readAndDispatch()) {
					display.sleep();
				}
			}
			gui.release();
		}
		display.dispose();
	}

	private class MqttConnectionThread extends Thread {

		private MqttConnectionThread() {
			super("MQTT-Connection-Thread");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				mqttClient.subscribeAppStatus();
				if (mqttClient.getClient() == null) {
					try {
						TimeUnit.SECONDS.sleep(configuration.getInt("mqtt.connect.retry.interval.secs", Defaults.MQTT_CONNECT_RETRY_INTERVAL_SECS)); // Wait between retries
					}
					catch (final InterruptedException e) {
						logger.log(Level.FINER, e.toString(), e);
						interrupt();
					}
					continue; // Retry
				}
				mqttClient.subscribeDeviceStatus();
				break;
			}
		}
	}

	private class ConnectThread extends Thread {
		private ConnectThread() {
			super("Connect-Thread");
		}

		@Override
		public void run() {
			final String protocol = configuration.getString("client.protocol").trim();
			if (protocol.equalsIgnoreCase(Protocol.MQTT.toString())) { // MQTT
				mqttClient.init(RouterLoggerClientGui.this);
				mqttConnectionThread = new MqttConnectionThread();
				mqttConnectionThread.start();
			}
			else if (protocol.toUpperCase().startsWith(Protocol.HTTP.toString().toUpperCase())) { // HTTP
				httpPollingThread = new HttpPollingThread(RouterLoggerClientGui.this);
				httpPollingThread.start();
			}
			else {
				logger.log(Level.INFO, Messages.get("err.invalid.protocol"), protocol);
			}
		}
	}

	private class ReleaseThread extends Thread {
		private ReleaseThread() {
			super("Release-Thread");
		}

		@Override
		public void run() {
			mqttClient.disconnect();
			if (mqttConnectionThread != null) {
				mqttConnectionThread.interrupt();
				try {
					mqttConnectionThread.join();
				}
				catch (final InterruptedException e) {
					logger.log(Level.FINER, e.toString(), e);
					interrupt();
				}
			}
			if (httpPollingThread != null) {
				httpPollingThread.interrupt();
				try {
					httpPollingThread.join();
				}
				catch (final InterruptedException e) {
					logger.log(Level.FINER, e.toString(), e);
					interrupt();
				}
			}
			printGoodbye();
		}
	}

	private void connect() {
		printWelcome();
		new ConnectThread().start();
	}

	private void release() {
		new ReleaseThread().start();
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);

		// Fix invisible (transparent) shell bug with some Linux distibutions
		if (SwtUtils.isGtk3() != null && !SwtUtils.isGtk3() && configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED)) {
			shell.setMinimized(true);
		}

		shell.setText(Messages.get("lbl.window.title"));
		shell.setImages(Images.getMainIcons());
	}

	@Override
	public int open() {
		final int code = super.open();

		// Fix invisible (transparent) shell bug with some Linux distibutions
		if ((SwtUtils.isGtk3() == null || SwtUtils.isGtk3()) && configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED)) {
			getShell().setMinimized(true);
		}

		return code;
	}

	@Override
	protected void handleShellCloseEvent() {
		final Event event = new Event();
		new CloseListener(this).handleEvent(event);
		if (event.doit) {
			super.handleShellCloseEvent();
		}
	}

	@Override
	protected Control createContents(final Composite parent) {
		trayIcon = new TrayIcon(this);

		menuBar = new MenuBar(this);

		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setSashWidth((int) (sashForm.getSashWidth() * SASH_MAGNIFICATION_FACTOR));
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		dataTable = new DataTable(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), this);

		console = new StyledTextConsole(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), true);
		final String fontDataString = configuration.getString("gui.console.font", true);
		if (!fontDataString.isEmpty()) {
			console.setFont(PreferenceConverter.readFontData(fontDataString));
		}
		console.setLimit(() -> configuration.getInt("gui.console.max.chars"));

		return parent;
	}

	@Override
	protected void initializeBounds() {/* Do not pack the shell */}

	protected void printWelcome() {
		if (logger.isLoggable(Level.INFO)) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintWriter pw = new PrintWriter(baos);
			pw.println(Messages.get("msg.startup.date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
			pw.println(" ____             _            _                                   ____ _ _            _");
			pw.println("|  _ \\ ___  _   _| |_ ___ _ __| |    ___   __ _  __ _  ___ _ __   / ___| (_) ___ _ __ | |_");
			pw.println("| |_) / _ \\| | | | __/ _ \\ '__| |   / _ \\ / _` |/ _` |/ _ \\ '__| | |   | | |/ _ \\ '_ \\| __|");
			pw.println("|  _ < (_) | |_| | ||  __/ |  | |__| (_) | (_| | (_| |  __/ |    | |___| | |  __/ | | | |_");
			pw.println("|_| \\_\\___/ \\__,_|\\__\\___|_|  |_____\\___/ \\__, |\\__, |\\___|_|     \\____|_|_|\\___|_| |_|\\__|");
			pw.println("                                          |___/ |___/");
			final Version version = Version.getInstance();
			pw.println(Messages.get("msg.welcome", Messages.get("msg.application.name"), Messages.get("msg.version", version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(version.getDate())), Messages.get("msg.website")));
			pw.close();
			logger.info(baos.toString());
		}

		logger.log(Level.CONFIG, Messages.get("msg.settings"), configuration);
	}

	@Override
	protected Layout getLayout() {
		return new GridLayout();
	}

	@Override
	protected void createTrimWidgets(final Shell shell) {/* Not needed */}

	protected void printGoodbye() {
		logger.info(Messages.get("msg.bye"));
	}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public SashForm getSashForm() {
		return sashForm;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public StyledTextConsole getConsole() {
		return console;
	}

	public boolean canCopyConsole() {
		return console.hasSelection() && (console.getScrollable().isFocusControl() || !dataTable.canCopy());
	}

	public boolean canSelectAllConsole() {
		return !console.isEmpty() && (console.getScrollable().isFocusControl() || !dataTable.canSelectAll());
	}

	public boolean canClearConsole() {
		return !console.isEmpty();
	}

	public AppStatus getCurrentStatus() {
		return currentStatus;
	}

	public AppStatus getPreviousStatus() {
		return previousStatus;
	}

	public void updateStatus(final AppStatus newStatus) {
		if (currentStatus == null || currentStatus.getStatus() == null || !currentStatus.getStatus().equals(newStatus.getStatus())) {
			previousStatus = currentStatus;
			currentStatus = newStatus;
			if (logger.isLoggable(Level.INFO)) {
				final LogRecord logRecord = new LogRecord(Level.INFO, Messages.get("lbl.status") + ": " + currentStatus.getStatus().getDescription());
				logRecord.setMillis(currentStatus.getTimestamp().getTime());
				logger.log(logRecord);
			}
			if (trayIcon != null) {
				trayIcon.updateTrayItem(currentStatus.getStatus());
				if (Status.WARNING.equals(currentStatus.getStatus())) {
					trayIcon.setShowToolTip(true);
				}
			}
		}
	}

	public void reconnectAfterConnectionLoss() {
		new Thread(() -> {
			// Disconnect
			mqttClient.disconnect();
			if (mqttConnectionThread != null) {
				mqttConnectionThread.interrupt();
				try {
					mqttConnectionThread.join();
				}
				catch (final InterruptedException e) {
					logger.log(Level.FINER, e.toString(), e);
					Thread.currentThread().interrupt();
				}
			}

			// Reconnect
			mqttClient.init(RouterLoggerClientGui.this);
			mqttConnectionThread = new MqttConnectionThread();
			mqttConnectionThread.start();
		}, "Reset-Thread").start();
	}

	public void restart() {
		// Disable "Restart..." menu item...
		menuBar.getFileRestartItem().setEnabled(false);

		new Thread(() -> {
			final Thread releaseThread = new ReleaseThread();
			releaseThread.start();
			try {
				releaseThread.join();
			}
			catch (final InterruptedException e) {
				logger.log(Level.FINE, e.toString(), e);
				Thread.currentThread().interrupt();
			}

			try {
				configuration.reload();
			}
			catch (final IOException e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
			new DisplayThreadExecutor(getShell()).execute(() -> {
				dataTable.reset();
				if (!logger.isLoggable(Level.FINE)) {
					console.clear();
				}
			});

			connect();

			// Enable "Restart..." menu item...
			new DisplayThreadExecutor(getShell()).execute(() -> menuBar.getFileRestartItem().setEnabled(true));
		}, "Reset-Thread").start();
	}

	public ThresholdsManager getThresholdsManager() {
		return thresholdsManager;
	}

}
