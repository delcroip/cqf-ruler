package org.opencds.cqf.ruler.cr.r4.provider;

//import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
//import org.hl7.fhir.r4.model.CarePlan;
//import org.hl7.fhir.r4.model.DomainResource;
//import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.ruler.cql.CqlConfig;
import org.opencds.cqf.ruler.cr.CrConfig;
import org.opencds.cqf.ruler.test.RestIntegrationTest;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//import ca.uhn.fhir.jpa.partition.SystemRequestDetails;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
		PlanDefinitionApplyProviderIT.class,
		CrConfig.class, CqlConfig.class }, properties = {
				"hapi.fhir.fhir_version=r4",
		})
public class PlanDefinitionApplyProviderIT extends RestIntegrationTest {

//	@Autowired
//	private PlanDefinitionApplyProvider planDefinitionApplyProvider;

	private Map<String, IBaseResource> planDefinitions;

	//@BeforeEach
	public void setup() throws Exception {
		uploadTests("valueset");
		uploadTests("library");
		planDefinitions = uploadTests("plandefinition");
	}

	//@Test
//	public void testPlanDefinitionApplyFormerSmoker() throws Exception {
//		DomainResource plandefinition = (DomainResource) planDefinitions.get("lcs-cds-patient-view");
//		// Patient First
//		uploadTests("test/plandefinition/LungCancerScreening/Former-Smoker/Patient");
//		Map<String, IBaseResource> resources = uploadTests("test/plandefinition/LungCancerScreening/Former-Smoker");
//		IBaseResource patient = resources.get("Former-Smoker");
//		Object isFormerSmoker = planDefinitionApplyProvider.applyPlanDefinition(new SystemRequestDetails(),
//				plandefinition.getIdElement(), patient.getIdElement().getIdPart(), null, null, null, null, null, null, null,
//				null);
//		assertTrue(isFormerSmoker instanceof CarePlan);
//	}

	@Test
	void testPlanDefinitionProcessor() {
		loadTransaction("ColorectalCancerScreeningCDS-bundle.json");
		loadTransaction("tests-should-screen-ccs-bundle.json");

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(this.getServerBase() + "/PlanDefinition/ColorectalCancerScreeningCDS/$apply?patient=should-screen-ccs");
			request.addHeader("Content-Type", "application/json");
			CloseableHttpResponse response = httpClient.execute(request);
			String jsonResponse = EntityUtils.toString(response.getEntity());
			String s = "s";
		} catch (IOException ioe) {
			fail(ioe.getMessage());
		}
	}
}
