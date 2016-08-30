package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.jface.preference.page.AbstractPreferencePage;
import it.albertus.router.client.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public abstract class RestartHeaderPreferencePage extends AbstractPreferencePage {

	@Override
	protected Control createHeader() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		TextFormatter.setBoldFontStyle(header);
		header.setText(Resources.get("lbl.preferences.restart.header"));
		return header;
	}

}
