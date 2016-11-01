package it.albertus.router.client.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.jface.preference.Preferences;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.preference.RouterLoggerClientPreferences;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;

public class PreferencesListener extends SelectionAdapter implements Listener {

	private final RouterLoggerGui gui;

	public PreferencesListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final Preferences preferences = new RouterLoggerClientPreferences(gui);
		try {
			preferences.openDialog(gui.getShell());
		}
		catch (final Exception e) {
			Logger.getInstance().log(e);
		}
		if (preferences.isRestartRequired()) {
			final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setText(Messages.get("lbl.window.title"));
			messageBox.setMessage(Messages.get("lbl.preferences.restart"));
			if (messageBox.open() == SWT.YES) {
				gui.restart();
			}
		}
	}

	@Override
	public void handleEvent(final Event event) {
		widgetSelected(null);
	}

}
