package org.opencds.cqf.ruler.cr.r4.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.StringType;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverter;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverterFactory;
import org.opencds.cqf.cql.evaluator.activitydefinition.r4.ActivityDefinitionProcessor;
import org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder;
import org.opencds.cqf.cql.evaluator.builder.EndpointConverter;
import org.opencds.cqf.cql.evaluator.builder.ModelResolverFactory;
import org.opencds.cqf.cql.evaluator.builder.TerminologyProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.data.DataProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.data.FhirModelResolverFactory;
import org.opencds.cqf.cql.evaluator.builder.data.FhirRestRetrieveProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.library.FhirRestLibraryContentProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.library.LibraryContentProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.library.TypedLibraryContentProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.terminology.FhirRestTerminologyProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.terminology.TypedTerminologyProviderFactory;
import org.opencds.cqf.cql.evaluator.cql2elm.util.LibraryVersionSelector;
import org.opencds.cqf.cql.evaluator.expression.ExpressionEvaluator;
import org.opencds.cqf.cql.evaluator.fhir.ClientFactory;
import org.opencds.cqf.cql.evaluator.fhir.adapter.r4.AdapterFactory;
import org.opencds.cqf.cql.evaluator.library.CqlFhirParametersConverter;
import org.opencds.cqf.cql.evaluator.library.LibraryProcessor;
import org.opencds.cqf.cql.evaluator.plandefinition.r4.OperationParametersParser;
import org.opencds.cqf.cql.evaluator.plandefinition.r4.PlanDefinitionProcessor;
import org.opencds.cqf.ruler.api.OperationProvider;
import org.opencds.cqf.ruler.cql.JpaFhirDal;
import org.opencds.cqf.ruler.cql.JpaFhirDalFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;

@Configurable
public class PlanApplyOperation implements OperationProvider {
    private final FhirContext fhirContext = FhirContext.forR4Cached();
    @Autowired
    JpaFhirDalFactory jpaFhirDalFactory;

    private final AdapterFactory adapterFactory = new AdapterFactory();
    private final FhirTypeConverter typeConverter = new FhirTypeConverterFactory().create(FhirVersionEnum.R4);
    private final ClientFactory clientFactory = new ClientFactory(fhirContext);
    private final LibraryVersionSelector versionSelector = new LibraryVersionSelector(adapterFactory);
    private final ModelResolverFactory modelResolverFactory = new FhirModelResolverFactory();

    @Operation(name = "$apply", idempotent = true, type = PlanDefinition.class)
    public CarePlan apply(RequestDetails theRequest, @IdParam IdType theId,
                          @OperationParam(name = "patient") String patientId,
                          @OperationParam(name = "encounter") String encounterId,
                          @OperationParam(name = "practitioner") String practitionerId,
                          @OperationParam(name = "organization") String organizationId,
                          @OperationParam(name = "userType") String userType,
                          @OperationParam(name = "userLanguage") String userLanguage,
                          @OperationParam(name = "userTaskContext") String userTaskContext,
                          @OperationParam(name = "setting") String setting,
                          @OperationParam(name = "settingContext") String settingContext) {
        CqlFhirParametersConverter parametersConverter = new CqlFhirParametersConverter(
                fhirContext, adapterFactory, typeConverter);
        TypedLibraryContentProviderFactory typedLibraryContentProviderFactory =
                new FhirRestLibraryContentProviderFactory(clientFactory, adapterFactory, versionSelector);
        LibraryContentProviderFactory contentProviderFactory = new LibraryContentProviderFactory(
                fhirContext, adapterFactory, Collections.singleton(typedLibraryContentProviderFactory),
                versionSelector);
        FhirRestRetrieveProviderFactory retrieveProviderFactory = new FhirRestRetrieveProviderFactory(
                fhirContext, clientFactory);
        DataProviderFactory dataProviderFactory = new DataProviderFactory(fhirContext,
                Collections.singleton(modelResolverFactory), Collections.singleton(retrieveProviderFactory));
        TypedTerminologyProviderFactory typedTerminologyProviderFactory = new FhirRestTerminologyProviderFactory(
                fhirContext, clientFactory);
        TerminologyProviderFactory terminologyProviderFactory =
                new org.opencds.cqf.cql.evaluator.builder.terminology.TerminologyProviderFactory(
                        fhirContext, Collections.singleton(typedTerminologyProviderFactory));
        EndpointConverter endpointConverter = new EndpointConverter(adapterFactory);
        LibraryProcessor libraryProcessor = new LibraryProcessor(fhirContext, parametersConverter,
                contentProviderFactory, dataProviderFactory, terminologyProviderFactory, endpointConverter,
                modelResolverFactory, CqlEvaluatorBuilder::new);
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(fhirContext, parametersConverter,
                contentProviderFactory, dataProviderFactory, terminologyProviderFactory, endpointConverter,
                modelResolverFactory, CqlEvaluatorBuilder::new);
        JpaFhirDal fhirDal = jpaFhirDalFactory.create(theRequest);
        ActivityDefinitionProcessor activityDefinitionProcessor = new ActivityDefinitionProcessor(
                fhirContext, fhirDal, libraryProcessor);
        OperationParametersParser parametersParser = new OperationParametersParser(adapterFactory, typeConverter);
        PlanDefinitionProcessor processor = new PlanDefinitionProcessor(fhirContext, fhirDal, libraryProcessor,
                expressionEvaluator, activityDefinitionProcessor, parametersParser);

        Endpoint defaultEndpoint = new Endpoint().setAddress(
                theRequest.getFhirServerBase()).setHeader(
                        Collections.singletonList(new StringType("Content-type: application/json")));
        boolean mergeNestedCarePlans = false;
        Parameters parameters = null;
        boolean useServerData = false;
        Bundle bundle = null;
        Parameters prefetchData = null;

        return processor.apply(theId, patientId, encounterId, practitionerId,
                organizationId, userType, userLanguage, userTaskContext, setting,
                settingContext, mergeNestedCarePlans, parameters, useServerData,
                bundle, prefetchData, defaultEndpoint, defaultEndpoint, defaultEndpoint);
    }

}
