package it.albertus.router.client.gui;

import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class CloseMessageBox {

	public interface Defaults {
		boolean GUI_CONFIRM_CLOSE = false;
	}

	private final MessageBox messageBox;

	private CloseMessageBox(Shell shell) {
		messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
		messageBox.setText(Resources.get("msg.confirm.close.text"));
		messageBox.setMessage(Resources.get("msg.confirm.close.message"));
	}

	public static MessageBox newInstance(Shell shell) {
		return new CloseMessageBox(shell).messageBox;
	}

	public static boolean show() {
		return RouterLoggerClientConfiguration.getInstance().getBoolean("gui.confirm.close", Defaults.GUI_CONFIRM_CLOSE);
	}

}
