package ste.uzzo.nb;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class FactsPanelTest {

    @Test
    void shows_the_proposed_facts_unchanged() {
        ProjectFacts proposed = new ProjectFacts("App", "1.0", "Does things.", "https://x/y", "21");

        then(new FactsPanel(proposed).facts()).isEqualTo(proposed);
    }

    @Test
    void absent_facts_are_shown_empty_and_stay_absent() {
        then(new FactsPanel(ProjectFacts.NONE).facts()).isEqualTo(ProjectFacts.NONE);
    }
}
