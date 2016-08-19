package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.AboutDialog;
import it.albertus.router.client.resources.Resources;
import it.albertus.util.Version;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AboutSelectionListener extends SelectionAdapter {

	private final IShellProvider gui;

	public AboutSelectionListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		final AboutDialog aboutDialog = new AboutDialog(gui.getShell());
		aboutDialog.setText(Resources.get("lbl.about.title"));
		aboutDialog.setMessage(Resources.get("msg.application.name") + ' ' + Resources.get("msg.version", Version.getInstance().getNumber(), Version.getInstance().getDate()));
		aboutDialog.setApplicationUrl(Resources.get("msg.website"));
		aboutDialog.setIconUrl(Resources.get("msg.info.icon.site"));
		aboutDialog.open();
	}

}
