package it.albertus.router.client.gui;

import it.albertus.router.client.Logger;
import it.albertus.router.client.RouterLoggerConfiguration;
import it.albertus.router.client.RouterLoggerStatus;
import it.albertus.router.client.Threshold;
import it.albertus.router.client.gui.listener.CloseListener;
import it.albertus.router.client.gui.listener.RestoreShellListener;
import it.albertus.router.client.resources.Resources;
import it.albertus.util.NewLine;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class TrayIcon {

	public interface Defaults {
		boolean GUI_MINIMIZE_TRAY = true;
		boolean GUI_TRAY_TOOLTIP = true;
	}

	private final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	private final RouterLoggerGui gui;

	private Tray tray;
	private TrayItem trayItem;
	private ToolTip toolTip;

	private Menu trayMenu;
	private MenuItem showMenuItem;
	private MenuItem exitMenuItem;

	/* To be accessed only from this class */
	private String toolTipText;
	private Image trayIcon;

	protected TrayIcon(final RouterLoggerGui gui) {
		this.gui = gui;
		gui.getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellIconified(ShellEvent e) {
				if (configuration.getBoolean("gui.minimize.tray", Defaults.GUI_MINIMIZE_TRAY)) {
					iconify();
				}
			}
		});
	}

	private Image getTrayIcon(final RouterLoggerStatus status) {
		if (status != null) {
			switch (status) {
			case STARTING:
			case CONNECTING:
			case DISCONNECTED:
				return Images.TRAY_ICON_INACTIVE;
			case RECONNECTING:
				return Images.TRAY_ICON_INACTIVE_CLOCK;
			case INFO:
			case WARNING:
				return Images.TRAY_ICON_ACTIVE_WARNING;
			case AUTHENTICATING:
				return Images.TRAY_ICON_ACTIVE_LOCK;
			case ERROR:
				return Images.TRAY_ICON_INACTIVE_ERROR;
			default:
				return Images.TRAY_ICON_ACTIVE;
			}
		}
		else {
			return Images.TRAY_ICON_ACTIVE;
		}
	}

	private void iconify() {
		if (tray == null || trayItem == null || trayItem.isDisposed()) {
			/* Inizializzazione */
			try {
				tray = gui.getShell().getDisplay().getSystemTray();

				if (tray != null) {
					trayItem = new TrayItem(tray, SWT.NONE);
					trayIcon = getTrayIcon(gui.getCurrentStatus());
					trayItem.setImage(trayIcon);
					toolTipText = getBaseToolTipText(gui.getCurrentStatus());
					trayItem.setToolTipText(toolTipText);

					toolTip = new ToolTip(gui.getShell(), SWT.BALLOON | SWT.ICON_WARNING);
					toolTip.setText(Resources.get("lbl.tray.tooltip.thresholds.reached"));
					toolTip.setVisible(false);
					toolTip.setAutoHide(true);
					toolTip.addListener(SWT.Selection, new RestoreShellListener(gui));
					trayItem.setToolTip(toolTip);

					trayMenu = new Menu(gui.getShell(), SWT.POP_UP);
					showMenuItem = new MenuItem(trayMenu, SWT.PUSH);
					showMenuItem.setText(Resources.get("lbl.tray.show"));
					showMenuItem.addListener(SWT.Selection, new RestoreShellListener(gui));
					trayMenu.setDefaultItem(showMenuItem);

					new MenuItem(trayMenu, SWT.SEPARATOR);

					exitMenuItem = new MenuItem(trayMenu, SWT.PUSH);
					exitMenuItem.setText(Resources.get("lbl.tray.close"));
					exitMenuItem.addSelectionListener(new CloseListener(gui));
					trayItem.addMenuDetectListener(new MenuDetectListener() {
						@Override
						public void menuDetected(MenuDetectEvent e) {
							trayMenu.setVisible(true);
						}
					});

					trayItem.addListener(SWT.DefaultSelection, new RestoreShellListener(gui));
				}
			}
			catch (Exception e) {
				Logger.getInstance().log(e);
			}
		}

		if (tray != null && !tray.isDisposed() && trayItem != null && !trayItem.isDisposed()) {
			gui.getShell().setVisible(false);
			trayItem.setVisible(true);
			trayItem.setImage(trayIcon); // Update icon
			gui.getShell().setMinimized(false);
		}
		else {
			Logger.getInstance().log("Tray not available.");
		}
	}

	public void updateTrayItem(final RouterLoggerStatus status) {
		updateTrayItem(status, null);
	}

	public void updateTrayItem(final RouterLoggerStatus status, final RouterData info) {
		if (trayItem != null && !trayItem.isDisposed()) {
			final StringBuilder sb = new StringBuilder(getBaseToolTipText(status));
			if (!configuration.getGuiImportantKeys().isEmpty() && info != null && info.getData() != null && !info.getData().isEmpty()) {
				for (final String key : configuration.getGuiImportantKeys()) {
					if (info.getData().containsKey(key)) {
						sb.append(NewLine.SYSTEM_LINE_SEPARATOR).append(key).append(": ").append(info.getData().get(key));
					}
				}
			}
			final String updatedToolTipText = sb.toString();
			if (!updatedToolTipText.equals(toolTipText) || (status != null && !getTrayIcon(status).equals(trayIcon))) {
				try {
					trayItem.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (!trayItem.isDisposed()) {
								if (!updatedToolTipText.equals(toolTipText)) {
									toolTipText = updatedToolTipText;
									trayItem.setToolTipText(toolTipText);
								}
								if (status != null && !getTrayIcon(status).equals(trayIcon)) {
									trayIcon = getTrayIcon(status);
									if (trayItem.getVisible() && gui != null && gui.getShell() != null && !gui.getShell().isDisposed() && !gui.getShell().getVisible()) {
										trayItem.setImage(trayIcon); // Only if visible!
									}
								}
							}
						}
					});
				}
				catch (SWTException se) {}
			}
		}
	}

	public void showBalloonToolTip(final Map<Threshold, String> thresholdsReached) {
		if (configuration.getBoolean("gui.tray.tooltip", Defaults.GUI_TRAY_TOOLTIP) && thresholdsReached != null && !thresholdsReached.isEmpty() && toolTip != null && trayItem != null && gui != null && gui.getShell() != null && !gui.getCurrentStatus().equals(gui.getPreviousStatus()) && !gui.getShell().isDisposed() && !trayItem.isDisposed() && !toolTip.isDisposed()) {
			final StringBuilder message = new StringBuilder();
			for (final Threshold threshold : thresholdsReached.keySet()) {
				message.append(threshold.getKey()).append('=').append(thresholdsReached.get(threshold)).append(NewLine.SYSTEM_LINE_SEPARATOR);
			}

			try {
				trayItem.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if (configuration.getBoolean("gui.tray.tooltip", Defaults.GUI_TRAY_TOOLTIP) && toolTip != null && trayItem != null && gui != null && gui.getShell() != null && !gui.getCurrentStatus().equals(gui.getPreviousStatus()) && !gui.getShell().isDisposed() && !trayItem.isDisposed() && !toolTip.isDisposed() && trayItem.getVisible() && !gui.getShell().getVisible()) {
							toolTip.setMessage(message.toString().trim());
							toolTip.setVisible(true);
						}
					}
				});
			}
			catch (SWTException se) {}
		}
	}

	private String getBaseToolTipText(final RouterLoggerStatus status) {
		final StringBuilder sb = new StringBuilder(Resources.get("lbl.tray.tooltip"));
		if (status != null) {
			sb.append(" (").append(status.getDescription()).append(')');
		}
		return sb.toString();
	}

	public Tray getTray() {
		return tray;
	}

	public TrayItem getTrayItem() {
		return trayItem;
	}

	public ToolTip getToolTip() {
		return toolTip;
	}

	public Menu getTrayMenu() {
		return trayMenu;
	}

	public MenuItem getShowMenuItem() {
		return showMenuItem;
	}

	public MenuItem getExitMenuItem() {
		return exitMenuItem;
	}

}