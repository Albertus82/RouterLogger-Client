package it.albertus.routerlogger.client.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class DeviceStatusDto implements Serializable {

	private static final long serialVersionUID = 1478970573356973998L;

	private Date timestamp;
	private Integer responseTime;
	private Map<String, String> data;
	private Set<ThresholdDto> thresholds;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Integer responseTime) {
		this.responseTime = responseTime;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public Set<ThresholdDto> getThresholds() {
		return thresholds;
	}

	public void setThresholds(Set<ThresholdDto> thresholds) {
		this.thresholds = thresholds;
	}

	public static class ThresholdDto implements Serializable {

		private static final long serialVersionUID = 231828478945570415L;

		private String name;
		private String key;
		private String type;
		private String value;
		private boolean excluded;
		private String detected;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public boolean isExcluded() {
			return excluded;
		}

		public void setExcluded(boolean excluded) {
			this.excluded = excluded;
		}

		public String getDetected() {
			return detected;
		}

		public void setDetected(String detected) {
			this.detected = detected;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ThresholdDto)) {
				return false;
			}
			ThresholdDto other = (ThresholdDto) obj;
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			}
			else if (!key.equals(other.key)) {
				return false;
			}
			if (type == null) {
				if (other.type != null) {
					return false;
				}
			}
			else if (!type.equals(other.type)) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			}
			else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

	}

}
