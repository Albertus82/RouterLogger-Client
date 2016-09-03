package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.resources.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

public class RestartSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public RestartSelectionListener(RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setMessage(Messages.get("msg.confirm.restart.message"));
		messageBox.setText(Messages.get("msg.confirm.restart.text"));
		if (messageBox.open() == SWT.YES) {
			gui.restart();
		}
	}

}
