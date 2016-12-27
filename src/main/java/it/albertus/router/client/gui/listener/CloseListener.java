package it.albertus.router.client.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.router.client.gui.CloseMessageBox;

public class CloseListener extends SelectionAdapter implements Listener {

	private final IShellProvider gui;

	public CloseListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (!CloseMessageBox.show() || confirmClose()) {
			gui.getShell().dispose();
		}
	}

	@Override
	public void handleEvent(Event event) {
		event.doit = !CloseMessageBox.show() || confirmClose();
	}

	private boolean confirmClose() {
		return CloseMessageBox.newInstance(gui.getShell()).open() == SWT.YES;
	}

}
