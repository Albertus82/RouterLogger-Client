package it.albertus.router.client.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.resources.Messages;

public class CloseMessageBox {

	public static class Defaults {
		public static final boolean GUI_CONFIRM_CLOSE = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private final MessageBox messageBox;

	private CloseMessageBox(final Shell shell) {
		messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
		messageBox.setText(Messages.get("msg.confirm.close.text"));
		messageBox.setMessage(Messages.get("msg.confirm.close.message"));
	}

	public static MessageBox newInstance(final Shell shell) {
		return new CloseMessageBox(shell).messageBox;
	}

	public static boolean show() {
		return RouterLoggerClientConfiguration.getInstance().getBoolean("gui.confirm.close", Defaults.GUI_CONFIRM_CLOSE);
	}

}
