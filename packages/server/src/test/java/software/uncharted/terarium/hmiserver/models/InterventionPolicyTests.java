package software.uncharted.terarium.hmiserver.models;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.Intervention;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.InterventionPolicy;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.StaticIntervention;

public class InterventionPolicyTests extends TerariumApplicationTests {

	@Test
	void testValidateWithNegative() throws Exception {
		final InterventionPolicy interventionPolicy = new InterventionPolicy();
		final List<Intervention> interventions = new ArrayList<Intervention>();
		final Intervention negativeIntervention = new Intervention();
		final List<StaticIntervention> staticInterventions = new ArrayList<StaticIntervention>();
		final StaticIntervention negativeStatic = new StaticIntervention();

		negativeStatic.setAppliedTo("beta");
		negativeStatic.setTimestep(-1);
		negativeStatic.setValue(10);

		staticInterventions.add(negativeStatic);

		negativeIntervention.setName("NegativeInterventionName");
		negativeIntervention.setStaticInterventions(staticInterventions);

		interventions.add(negativeIntervention);

		interventionPolicy.setName("testNegativeFails");
		interventionPolicy.setDescription("mydescription");
		interventionPolicy.setInterventions(interventions);

		Assertions.assertThrowsExactly(
			Exception.class,
			() -> interventionPolicy.validateInterventionPolicy(),
			"The intervention NegativeInterventionName has a timestep -1 which is less than 0."
		);
	}

	@Test
	void testValidateWithDuplicateInIntervention() throws Exception {
		final InterventionPolicy interventionPolicy = new InterventionPolicy();
		final List<Intervention> interventions = new ArrayList<Intervention>();
		final Intervention intervention = new Intervention();
		final List<StaticIntervention> staticInterventions = new ArrayList<StaticIntervention>();
		final StaticIntervention staticOne = new StaticIntervention();
		final StaticIntervention staticTwo = new StaticIntervention();

		staticOne.setAppliedTo("beta");
		staticOne.setTimestep(1);
		staticOne.setValue(10);

		staticTwo.setAppliedTo("beta");
		staticTwo.setTimestep(1);
		staticTwo.setValue(20);

		staticInterventions.add(staticOne);
		staticInterventions.add(staticTwo);

		intervention.setName("DuplicateName");
		intervention.setStaticInterventions(staticInterventions);

		interventions.add(intervention);

		interventionPolicy.setName("testDuplicateInInterventionFails");
		interventionPolicy.setDescription("mydescription");
		interventionPolicy.setInterventions(interventions);

		Assertions.assertThrowsExactly(
			Exception.class,
			() -> interventionPolicy.validateInterventionPolicy(),
			"The intervention DuplicateName has duplicate applied to: beta and time: 1 pairs."
		);
	}

	@Test
	void testValidateWithDuplicateInPolicy() throws Exception {
		final InterventionPolicy interventionPolicy = new InterventionPolicy();
		final List<Intervention> interventions = new ArrayList<Intervention>();
		final Intervention interventionOne = new Intervention();
		final Intervention interventionTwo = new Intervention();
		final List<StaticIntervention> staticInterventionsOne = new ArrayList<StaticIntervention>();
		final List<StaticIntervention> staticInterventionsTwo = new ArrayList<StaticIntervention>();
		final StaticIntervention staticOne = new StaticIntervention();
		final StaticIntervention staticTwo = new StaticIntervention();

		staticOne.setAppliedTo("beta");
		staticOne.setTimestep(1);
		staticOne.setValue(10);

		staticTwo.setAppliedTo("beta");
		staticTwo.setTimestep(1);
		staticTwo.setValue(20);

		staticInterventionsOne.add(staticOne);
		staticInterventionsTwo.add(staticTwo);

		interventionOne.setName("IntOne");
		interventionOne.setStaticInterventions(staticInterventionsOne);

		interventionTwo.setName("IntTwo");
		interventionTwo.setStaticInterventions(staticInterventionsTwo);

		interventions.add(interventionOne);
		interventions.add(interventionTwo);

		interventionPolicy.setName("testDuplicateInPolicyFails");
		interventionPolicy.setDescription("mydescription");
		interventionPolicy.setInterventions(interventions);

		Assertions.assertThrowsExactly(
			Exception.class,
			() -> interventionPolicy.validateInterventionPolicy(),
			"The intervention IntTwo has duplicate applied to: beta and time: 1 pairs."
		);
	}

	@Test
	void testValidateSuccess() throws Exception {
		final InterventionPolicy interventionPolicy = new InterventionPolicy();
		final List<Intervention> interventions = new ArrayList<Intervention>();
		final Intervention interventionOne = new Intervention();
		final Intervention interventionTwo = new Intervention();
		final List<StaticIntervention> staticInterventionsOne = new ArrayList<StaticIntervention>();
		final List<StaticIntervention> staticInterventionsTwo = new ArrayList<StaticIntervention>();
		final StaticIntervention staticOne = new StaticIntervention();
		final StaticIntervention staticTwo = new StaticIntervention();

		staticOne.setAppliedTo("beta");
		staticOne.setTimestep(1);
		staticOne.setValue(10);

		staticTwo.setAppliedTo("beta");
		staticTwo.setTimestep(2);
		staticTwo.setValue(20);

		staticInterventionsOne.add(staticOne);
		staticInterventionsTwo.add(staticTwo);

		interventionOne.setName("IntOne");
		interventionOne.setStaticInterventions(staticInterventionsOne);

		interventionTwo.setName("IntTwo");
		interventionTwo.setStaticInterventions(staticInterventionsTwo);

		interventions.add(interventionOne);
		interventions.add(interventionTwo);

		interventionPolicy.setName("testNoDuplicatePass");
		interventionPolicy.setDescription("mydescription");
		interventionPolicy.setInterventions(interventions);

		Assertions.assertTrue(interventionPolicy.validateInterventionPolicy());
	}
}
