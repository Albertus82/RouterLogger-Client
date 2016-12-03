package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class SelectAllMenuBarSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public SelectAllMenuBarSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canSelectAllConsole()) {
			gui.getConsole().getScrollable().selectAll();
		}
		else if (gui.getDataTable().canSelectAll()) {
			gui.getDataTable().getTable().selectAll();
		}
	}

}
