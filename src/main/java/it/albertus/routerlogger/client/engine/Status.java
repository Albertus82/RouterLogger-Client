package it.albertus.routerlogger.client.engine;

import it.albertus.routerlogger.client.resources.Messages;

public enum Status {
	STARTING,
	CONNECTING,
	AUTHENTICATING,
	OK,
	INFO,
	WARNING,
	DISCONNECTING,
	DISCONNECTED,
	RECONNECTING,
	ERROR,
	CLOSED,
	ABEND;

	private static final String RESOURCE_KEY_PREFIX = "lbl.status.";

	private final String resourceKey;

	private Status() {
		this.resourceKey = RESOURCE_KEY_PREFIX + name().toLowerCase().replace('_', '.');
	}

	public String getDescription() {
		return Messages.get(resourceKey);
	}

}
