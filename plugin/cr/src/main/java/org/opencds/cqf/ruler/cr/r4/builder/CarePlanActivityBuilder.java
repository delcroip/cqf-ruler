package org.opencds.cqf.ruler.cr.r4.builder;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public class CarePlanActivityBuilder extends BaseBuilder<CarePlan.CarePlanActivityComponent> {

    public CarePlanActivityBuilder() {
        super(new CarePlan.CarePlanActivityComponent());
    }

    public CarePlanActivityBuilder buildOutcomeConcept(List<CodeableConcept> concepts) {
        complexProperty.setOutcomeCodeableConcept(concepts);
        return this;
    }

    public CarePlanActivityBuilder buildOutcomeConcept(CodeableConcept concept) {
        if (!complexProperty.hasOutcomeCodeableConcept()) {
            complexProperty.setOutcomeCodeableConcept(new ArrayList<>());
        }

        complexProperty.addOutcomeCodeableConcept(concept);
        return this;
    }

    public CarePlanActivityBuilder buildOutcomeReference(List<Reference> references) {
        complexProperty.setOutcomeReference(references);
        return this;
    }

    public CarePlanActivityBuilder buildOutcomeReference(Reference reference) {
        if (!complexProperty.hasOutcomeReference()) {
            complexProperty.setOutcomeReference(new ArrayList<>());
        }

        complexProperty.addOutcomeReference(reference);
        return this;
    }

    public CarePlanActivityBuilder buildProgress(List<Annotation> annotations) {
        complexProperty.setProgress(annotations);
        return this;
    }

    public CarePlanActivityBuilder buildProgress(Annotation annotation) {
        if (!complexProperty.hasProgress()) {
            complexProperty.setProgress(new ArrayList<>());
        }

        complexProperty.addProgress(annotation);
        return this;
    }

    public CarePlanActivityBuilder buildReference(Reference reference) {
        complexProperty.setReference(reference);
        return this;
    }

    public CarePlanActivityBuilder buildReferenceTarget(Resource resource) {
        complexProperty.setReferenceTarget(resource);
        return this;
    }

    public CarePlanActivityBuilder buildDetail(CarePlan.CarePlanActivityDetailComponent detail) {
        complexProperty.setDetail(detail);
        return this;
    }
}
