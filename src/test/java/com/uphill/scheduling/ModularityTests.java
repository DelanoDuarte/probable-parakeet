package com.uphill.scheduling;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the modular architecture is actually respected.
 *
 * <p>{@link ApplicationModules#verify()} fails the build if any module reaches into
 * another module's internals (only public API packages may be referenced across
 * modules) or if a cyclic dependency is introduced between modules. This is what
 * keeps the boundaries described in the README from rotting over time.
 *
 * <p>{@code writesDocumentation()} additionally emits PlantUML component diagrams
 * and a module canvas under {@code build/spring-modulith-docs} — handy for the
 * "present it to your colleagues" part of the challenge.
 */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(SchedulingApplication.class);

    @Test
    void verifiesModuleBoundaries() {
        modules.verify();
    }

    @Test
    void writesDocumentation() {
        // Write under Gradle's build directory rather than the Modulith default (target/).
        Documenter.DiagramOptions diagramOptions = Documenter.DiagramOptions.defaults();
        Documenter.CanvasOptions canvasOptions = Documenter.CanvasOptions.defaults();

        new Documenter(modules)
                .writeModulesAsPlantUml(diagramOptions)
                .writeIndividualModulesAsPlantUml(diagramOptions)
                .writeModuleCanvases(canvasOptions);
    }
}
