package com.meral.restaws.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.meral.restaws.NameDateIpUaId;
import com.meral.restaws.core.Saying;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
	private final String template;
	private final String defaultName;
	private final AtomicLong counter;

	private final String topicName = "mytopic";
	private final String projectName = "myhelloworldprojectmeral";
	private final String fullName = "projects/" + projectName + "/topics/"
			+ topicName;

	private final ObjectMapper JSON = new ObjectMapper();

	private int requestCounter = 0;

	// Bigquery bg;
	Pubsub ps;

	// public HelloWorldResource(String template, String defaultName, Bigquery
	// bg) {
	// this.template = template;
	// this.defaultName = defaultName;
	// this.bg = bg;
	// this.counter = new AtomicLong();
	// }

	public HelloWorldResource(String template, String defaultName, Pubsub ps) {
		this.template = template;
		this.defaultName = defaultName;
		this.ps = ps;
		this.counter = new AtomicLong();
	}

	@GET
	@Timed
	public Saying sayHello(@QueryParam("name") Optional<String> name,
			@HeaderParam("User-Agent") String userAgent,
			@Context HttpServletRequest request) throws IOException {

		String jstr = JSON.writeValueAsString(makeNDIUI(name,
				request.getRemoteAddr(), userAgent));
		System.out.println("jsrt: " + jstr);
//		Map<String, Object> rowData = null;
//		rowData = new ObjectMapper().readValue(jstr, HashMap.class);
		// Publish messages to PubSub topic:
		PubsubMessage pubsubMessage = new PubsubMessage();
		// You need to base64-encode your message with
		// PubsubMessage#encodeData() method.
		pubsubMessage.encodeData(jstr.getBytes("UTF-8"));
		List<PubsubMessage> messages = ImmutableList.of(pubsubMessage);
		PublishRequest publishRequest = new PublishRequest()
				.setMessages(messages);
		PublishResponse publishResponse = ps.projects().topics()
				.publish(fullName, publishRequest).execute();
		List<String> messageIds = publishResponse.getMessageIds();
		if (messageIds != null) {
			for (String messageId : messageIds) {
				System.out.println("messageId: " + messageId);
			}
		}

		final String value = String.format(template, name.or(defaultName));
		return new Saying(counter.incrementAndGet(), value);
	}

	private byte[] makeMyJSON(Optional<String> name, String address,
			String userAgent) {
		NameDateIpUaId ndiui = makeNDIUI(name, address, userAgent);
		return makeMyJSONFromNDIUI(ndiui);
	}

	private byte[] makeMyJSONFromNDIUI(NameDateIpUaId ndiui) {
		byte[] bytes = null;
		try {
			bytes = JSON.writeValueAsBytes(ndiui);
			System.out.println("Example JSON for report: "
					+ JSON.writeValueAsString(ndiui));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	private NameDateIpUaId makeNDIUI(Optional<String> name, String address,
			String userAgent) {
		String nameToLog;
		if (name.isPresent()) {
			nameToLog = name.get();
		} else {
			nameToLog = "Stranger";
		}
		return new NameDateIpUaId(nameToLog, new Date(), address, userAgent);
	}
}