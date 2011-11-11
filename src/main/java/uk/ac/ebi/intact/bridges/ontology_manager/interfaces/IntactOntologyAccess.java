package uk.ac.ebi.intact.bridges.ontology_manager.interfaces;

import psidev.psi.tools.ontology_manager.interfaces.OntologyAccessTemplate;

/**
 * Extension of OntologyAcessTemplate for intact
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/11/11</pre>
 */

public interface IntactOntologyAccess extends OntologyAccessTemplate<IntactOntologyTermI> {

    public String getOntologyID();
    public String getDatabaseIdentifier();
    public String getParentFromOtherOntology();
}
