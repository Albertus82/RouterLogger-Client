package it.albertus.router.client.gui.listener;

import it.albertus.jface.preference.Preferences;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.preference.RouterLoggerClientPreferences;
import it.albertus.router.client.resources.Resources;
import it.albertus.router.client.util.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

public class PreferencesSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public PreferencesSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final Preferences preferences = new RouterLoggerClientPreferences(gui);
		try {
			preferences.open(gui.getShell());
		}
		catch (final Exception e) {
			Logger.getInstance().log(e);
		}
		if (preferences.isRestartRequired()) {
			final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setText(Resources.get("lbl.window.title"));
			messageBox.setMessage(Resources.get("lbl.preferences.restart"));
			if (messageBox.open() == SWT.YES) {
				gui.restart();
			}
		}
	}

}
