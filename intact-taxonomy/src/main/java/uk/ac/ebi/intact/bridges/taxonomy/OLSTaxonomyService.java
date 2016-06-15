/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.bridges.taxonomy;

import org.apache.commons.lang3.text.WordUtils;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * OLS integration for accession the NCBI Taxonomy.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class OLSTaxonomyService implements TaxonomyService {

    public static final String NCBITAXON = "ncbitaxon";
    // match things like: Mus musculus (Mouse)
    private Pattern NAME_PATTERN = Pattern.compile("(.+)\\((.+)\\)");

    private OLSClient olsClient;

    public OLSTaxonomyService() {
        this.olsClient = new OLSClient(new OLSWsConfigProd());
    }

    public TaxonomyTerm getTaxonomyTerm(int taxid) throws TaxonomyServiceException {

        TaxonomyUtils.isSupportedTaxid(taxid);

        TaxonomyTerm taxonomyTerm = null;

        if (taxid == -1) {
            taxonomyTerm = new TaxonomyTerm(-1);
            taxonomyTerm.setScientificName("In vitro");
            taxonomyTerm.setCommonName("In vitro");
        } else if (taxid == -2) {
            taxonomyTerm = new TaxonomyTerm(-2);
            taxonomyTerm.setScientificName("Chemical synthesis");
            taxonomyTerm.setCommonName("Chemical synthesis");
        } else if (taxid == -3) {
            taxonomyTerm = new TaxonomyTerm(-3);
            taxonomyTerm.setScientificName("Unknown");
            taxonomyTerm.setCommonName("Unknown");
        } else if (taxid == -4) {
            taxonomyTerm = new TaxonomyTerm(-4);
            taxonomyTerm.setScientificName("In vivo");
            taxonomyTerm.setCommonName("In vivo");
        } else if (taxid == -5) {
            taxonomyTerm = new TaxonomyTerm(-5);
            taxonomyTerm.setScientificName("In Silico");
            taxonomyTerm.setCommonName("In Silico");
        }

        if (taxonomyTerm == null) {

            taxonomyTerm = new TaxonomyTerm(taxid);

            Identifier identifier = new Identifier("NCBITaxon:" + String.valueOf(taxid), Identifier.IdentifierType.OBO);
            Term term = olsClient.getTermById(identifier, "ncbitaxon");
            String scientificName = term.getLabel();
            String commonName = term.getLabel();

            for (Map.Entry<String, String> entry : term.getOboSynonyms().entrySet()) {
                if (entry.getValue().equals("genbank_common_name")) {
                    commonName = WordUtils.capitalize(entry.getKey());
                }
            }
            taxonomyTerm.setCommonName(commonName);
            taxonomyTerm.setScientificName(scientificName);
        }

        return taxonomyTerm;
    }

    public void retrieveChildren(TaxonomyTerm term, boolean recursively) throws TaxonomyServiceException {
        // This could be achieved by using getting the relationship of a term and iteratively getting the terms details...
        throw new UnsupportedOperationException();
    }

    public void retrieveParents(TaxonomyTerm term, boolean recursively) throws TaxonomyServiceException {
        throw new UnsupportedOperationException();
    }

    public List<TaxonomyTerm> getTermChildren(int taxid) throws TaxonomyServiceException {
        throw new UnsupportedOperationException();
    }

    public List<TaxonomyTerm> getTermParent(int taxid) throws TaxonomyServiceException {
        throw new UnsupportedOperationException();
    }

    public String getSourceDatabaseMiRef() {
        return "MI:0942"; // cheating here, there is no OLS database in the MI ontology ... and Newt is dead :(
    }

    public String getSourceDatabaseName() {
        return "uniprot taxonomy";  // cheating here, there is no OLS database in the MI ontology ... and Newt is dead :(
    }
}
