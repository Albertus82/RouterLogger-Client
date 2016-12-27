package it.albertus.router.client.mqtt.listener;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.dto.transformer.ThresholdsTransformer;
import it.albertus.router.client.engine.ThresholdsReached;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.mqtt.BaseMqttClient;

public class ThresholdsMqttMessageListener implements IMqttMessageListener {

	private final RouterLoggerClientGui gui;

	public ThresholdsMqttMessageListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		final ThresholdsDto dto = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), ThresholdsDto.class);

		if (dto != null && dto.getReached() != null && !dto.getReached().isEmpty()) {
			final ThresholdsReached thresholdsReached = ThresholdsTransformer.fromDto(dto);

			gui.getThresholdsManager().updateDataTable(thresholdsReached);

			if (!message.isRetained()) {
				gui.getThresholdsManager().printThresholdsReached(thresholdsReached);
			}
		}
	}

}
