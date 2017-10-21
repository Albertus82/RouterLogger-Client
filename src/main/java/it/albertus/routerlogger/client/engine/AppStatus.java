package it.albertus.routerlogger.client.engine;

import java.io.Serializable;
import java.util.Date;

public class AppStatus implements Serializable {

	private static final long serialVersionUID = -5574131723102221930L;

	private final Date timestamp;
	private final Status status;

	public AppStatus(final Status status, final Date timestamp) {
		this.timestamp = timestamp;
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "RouterLoggerStatus [timestamp=" + timestamp + ", status=" + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AppStatus)) {
			return false;
		}
		final AppStatus other = (AppStatus) obj;
		return status == other.status;
	}

}
