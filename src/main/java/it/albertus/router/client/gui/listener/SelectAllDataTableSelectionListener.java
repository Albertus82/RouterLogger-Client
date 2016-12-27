package it.albertus.router.client.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerClientGui;

public class SelectAllDataTableSelectionListener extends SelectionAdapter {

	private final RouterLoggerClientGui gui;

	public SelectAllDataTableSelectionListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canSelectAll()) {
			dataTable.getTable().selectAll();
		}
	}

}
