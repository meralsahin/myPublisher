package com.meral.restaws;


import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.api.services.pubsub.model.ListTopicsResponse;
import com.google.api.services.pubsub.model.Topic;
import com.meral.restaws.health.TemplateHealthCheck;
import com.meral.restaws.resources.HelloWorldResource;

public class HelloWorldApplication extends Application<RestGCPConfiguration> {
	
	// Default factory method.
    public static Pubsub createPubsubClient() throws IOException {
        return createPubsubClient(Utils.getDefaultTransport(),
                Utils.getDefaultJsonFactory());
    }

    // A factory method that allows you to use your own HttpTransport
    // and JsonFactory.
    public static Pubsub createPubsubClient(HttpTransport httpTransport,
            JsonFactory jsonFactory) throws IOException {
        Preconditions.checkNotNull(httpTransport);
        Preconditions.checkNotNull(jsonFactory);
        GoogleCredential credential = GoogleCredential.getApplicationDefault(
                httpTransport, jsonFactory);
        // In some cases, you need to add the scope explicitly.
        if (credential.createScopedRequired()) {
            credential = credential.createScoped(PubsubScopes.all());
        }
        // Please use custom HttpRequestInitializer for automatic
        // retry upon failures.  We provide a simple reference
        // implementation in the "Retry Handling" section.
        HttpRequestInitializer initializer =
                new RetryHttpInitializerWrapper(credential);
        return new Pubsub.Builder(httpTransport, jsonFactory, initializer).setApplicationName("PubSub Sample")
               .build();
    }
	
    public static void main(String[] args) throws Exception {
    	//GCP stuff:
    	
        
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<RestGCPConfiguration> bootstrap) {
        // nothing to do yet
    }

   
    @Override
    public void run(RestGCPConfiguration configuration,
                    Environment environment) throws IOException {
    	HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = null;
        GoogleCredential credentialPS = null;
		try {
			credential = GoogleCredential.getApplicationDefault(transport, jsonFactory);
			credentialPS = GoogleCredential.getApplicationDefault(transport, jsonFactory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Depending on the environment that provides the default credentials (e.g. Compute Engine, App
        // Engine), the credentials may require us to specify the scopes we need explicitly.
        // Check for this case, and inject the Bigquery scope if required.
        if (credential.createScopedRequired()) {
            credential = credential.createScoped(BigqueryScopes.all());
        }
        if (credentialPS.createScopedRequired()) {
            credentialPS = credentialPS.createScoped(PubsubScopes.all());
        }
      
        Pubsub ps = null;
        try {
			ps = createPubsubClient();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Create topic for this application where publishers write to, only if it doesn't already exist:
        String topicName = "mytopic";
        String projectName = "myhelloworldprojectmeral";
        String fullName = "projects/" + projectName + "/topics/" + topicName;
        boolean topicAlreadyExists = false;
        Pubsub.Projects.Topics.List listMethod =
                ps.projects().topics().list("projects/" + projectName);
        String nextPageToken = null;
        ListTopicsResponse response;
        do {
            if (nextPageToken != null) {
                listMethod.setPageToken(nextPageToken);
            }
            response = listMethod.execute();
            List<Topic> topics = response.getTopics();
            if (topics != null) {
                for (Topic topic : topics) {
                    System.out.println("Found topic: " + topic.getName());
                    if (topic.getName().equals(fullName)) {
                    	topicAlreadyExists = true;
                    }
                }
            }
            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);
        if (!topicAlreadyExists) {
        	Topic newTopic = null;
            try {
    			newTopic = ps.projects().topics()
    			        .create("projects/" + projectName + "/topics/" + topicName, new Topic())
    			        .execute();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            System.out.println("Created: " + newTopic.getName());
        }
    	
        final HelloWorldResource resource = new HelloWorldResource(
            configuration.getTemplate(),
            configuration.getDefaultName(),
            ps
        );
        final TemplateHealthCheck healthCheck =
            new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
    }
}
