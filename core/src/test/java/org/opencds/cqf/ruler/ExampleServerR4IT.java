package org.opencds.cqf.ruler;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.util.BundleUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class, properties = {
		"spring.batch.job.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:dbr4",
		"hapi.fhir.enable_repository_validating_interceptor=true",
		"hapi.fhir.fhir_version=r4",
		"hapi.fhir.subscription.websocket_enabled=true",
		"hapi.fhir.mdm_enabled=true",
		// Override is currently required when using MDM as the construction of the MDM
		// beans are ambiguous as they are constructed multiple places. This is evident
		// when running in a spring boot environment
		"spring.main.allow-bean-definition-overriding=true"
})
public class ExampleServerR4IT {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerR4IT.class);
	private IGenericClient ourClient;
	private FhirContext ourCtx;

	@LocalServerPort
	private int port;

	@Test
	@Order(0)
	void testCreateAndRead() {
		String methodName = "testCreateAndRead";
		ourLog.info("Entering " + methodName + "()...");

		Patient pt = new Patient();
		pt.setActive(true);
		pt.getBirthDateElement().setValueAsString("2020-01-01");
		pt.addIdentifier().setSystem("http://foo").setValue("12345");
		pt.addName().setFamily(methodName);
		IIdType id = ourClient.create().resource(pt).execute().getId();

		Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
		assertEquals(methodName, pt2.getName().get(0).getFamily());

		// Wait until the MDM message has been processed
		// await().until(() -> {
		// sleep(1000);
		// return getGoldenResourcePatient() != null;
		// });
		// Patient goldenRecord = getGoldenResourcePatient();

		// // Verify that a golden record Patient was created
		// assertNotNull(goldenRecord.getMeta().getTag("http://hapifhir.io/fhir/NamingSystem/mdm-record-status",
		// "GOLDEN_RECORD"));
	}

	@SuppressWarnings("unused")
	private List<Patient> getPatients() {
		Bundle bundle = ourClient.search().forResource(Patient.class)
				.cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute();
		List<Patient> retVal = BundleUtil.toListOfResourcesOfType(ourCtx, bundle, Patient.class);
		retVal.sort(comparing(o -> ((Patient) o).getMeta().getLastUpdated()).reversed());
		return retVal;
	}

	// private Patient getGoldenResourcePatient() {
	// Bundle bundle = ourClient.search()
	// .forResource(Patient.class)
	// .withTag("http://hapifhir.io/fhir/NamingSystem/mdm-record-status",
	// "GOLDEN_RECORD")
	// .cacheControl(new
	// CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute();
	// if (bundle.getEntryFirstRep() != null) {
	// return (Patient) bundle.getEntryFirstRep().getResource();
	// } else {
	// return null;
	// }
	// }

	// @Test
	// @Order(1)
	// public void testWebsocketSubscription() throws Exception {
	// /*
	// * Create subscription
	// */
	// Subscription subscription = new Subscription();
	// subscription.setReason("Monitor new neonatal function (note, age will be
	// determined by the monitor)");
	// subscription.setStatus(Subscription.SubscriptionStatus.REQUESTED);
	// subscription.setCriteria("Observation?status=final");

	// Subscription.SubscriptionChannelComponent channel = new
	// Subscription.SubscriptionChannelComponent();
	// channel.setType(Subscription.SubscriptionChannelType.WEBSOCKET);
	// channel.setPayload("application/json");
	// subscription.setChannel(channel);

	// MethodOutcome methodOutcome =
	// ourClient.create().resource(subscription).execute();
	// IIdType mySubscriptionId = methodOutcome.getId();

	// // Wait for the subscription to be activated
	// await().until(() -> activeSubscriptionCount() == 3);

	// /*
	// * Attach websocket
	// */

	// WebSocketClient myWebSocketClient = new WebSocketClient();
	// SocketImplementation mySocketImplementation = new
	// SocketImplementation(mySubscriptionId.getIdPart(), EncodingEnum.JSON);

	// myWebSocketClient.start();
	// URI echoUri = new URI("ws://localhost:" + port + "/websocket");
	// ClientUpgradeRequest request = new ClientUpgradeRequest();
	// ourLog.info("Connecting to : {}", echoUri);
	// Future<Session> connection =
	// myWebSocketClient.connect(mySocketImplementation, echoUri, request);
	// Session session = connection.get(2, TimeUnit.SECONDS);

	// ourLog.info("Connected to WS: {}", session.isOpen());

	// /*
	// * Create a matching resource
	// */
	// Observation obs = new Observation();
	// obs.setStatus(Observation.ObservationStatus.FINAL);
	// ourClient.create().resource(obs).execute();

	// // Give some time for the subscription to deliver
	// sleep(2000);

	// /*
	// * Ensure that we receive a ping on the websocket
	// */
	// waitForSize(1, () -> mySocketImplementation.myPingCount);

	// /*
	// * Clean up
	// */
	// ourClient.delete().resourceById(mySubscriptionId).execute();
	// }

	// private int activeSubscriptionCount() {
	// return
	// ourClient.search().forResource(Subscription.class).where(Subscription.STATUS.exactly().code("active")).cacheControl(new
	// CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute().getEntry().size();
	// }

	@BeforeEach
	void beforeEach() {

		ourCtx = FhirContext.forCached(FhirVersionEnum.R4);
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		// ourClient.registerInterceptor(new LoggingInterceptor(false));
	}

	@BeforeAll
	public static void setup() {
		Awaitility.setDefaultTimeout(30, TimeUnit.SECONDS);
	}
}
