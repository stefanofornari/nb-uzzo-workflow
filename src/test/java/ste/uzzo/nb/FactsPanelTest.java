/*
 * Copyright 2026 the original author or authors from the Uzzo and Uzzo Workflow project
 * (https://stefanofornari.github.io/uzzo-workflow).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
