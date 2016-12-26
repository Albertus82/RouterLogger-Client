package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.RouterLoggerClientGui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;

public abstract class ClearSelectionListener implements SelectionListener {

	protected final RouterLoggerClientGui gui;

	public ClearSelectionListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}

	protected boolean confirm(final String dialogTitle, final String dialogMessage) {
		final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setMessage(dialogMessage);
		messageBox.setText(dialogTitle);
		return messageBox.open() == SWT.YES;
	}

}
