package ste.uzzo.nb;

/** Lets the user review the project facts before the knowledge base is created. */
interface FactsPrompt {

    /** Returns the facts to use, or null when the user cancels. */
    ProjectFacts ask(ProjectFacts proposed);
}
