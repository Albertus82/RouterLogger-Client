package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.resources.Resources;

import org.eclipse.swt.events.SelectionEvent;

public class ClearDataTableSelectionListener extends ClearSelectionListener {

	public ClearDataTableSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canClear()) {
			if (confirm(Resources.get("msg.confirm.clear.table.text"), Resources.get("msg.confirm.clear.table.message")) && dataTable.canClear()) {
				dataTable.clear();
			}
		}
	}

}
