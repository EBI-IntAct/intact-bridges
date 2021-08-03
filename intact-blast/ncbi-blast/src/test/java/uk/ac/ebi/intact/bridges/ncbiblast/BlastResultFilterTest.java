package uk.ac.ebi.intact.bridges.ncbiblast;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.ncbiblast.model.BlastProtein;

import java.io.InputStream;
import java.util.List;

/**
 * BlastResultFilter tester
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18-Mar-2010</pre>
 */

public class BlastResultFilterTest {
    InputStream results;
    ProteinNCBIBlastService blastService;
    BlastResultFilter filter;
    private final String sequence = "MFAVMRIDNDDCRSDFRRKMRPKCEFICKYCQRRFTKPYNLMIHERTHKSPEITYSCEVCGKYFKQRDNLRQHRCSQCVWR";

    public BlastResultFilterTest(){
        try {
            blastService = new ProteinNCBIBlastService("ntoro@ebi.ac.uk");
            results = blastService.getResultsOfBlastOnUniprot(sequence);

            filter = new BlastResultFilter(results);
        } catch (BlastServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void getResultWithoutFiltering(){

        filter.readResultsWithoutFiltering();

        List<BlastProtein> filteredResults = filter.getMatchingEntries();

        Assert.assertFalse(filteredResults.isEmpty());
        Assert.assertEquals(100, filteredResults.size());
    }

    @Test
    public void getResult_FilterIdentity(){

        filter.filterResultsWithIdentity((float) 100);

        List<BlastProtein> filteredResults = filter.getMatchingEntries();

        Assert.assertEquals(false, filteredResults.isEmpty());

        for (BlastProtein prot : filteredResults){
            System.out.println(prot.getAccession());
            System.out.println(prot.getIdentity());
        }
        Assert.assertEquals(41, filteredResults.size());
    }

    @Test
    public void getResult_FilterOrganism(){

        filter.filterResultsWithOrganism("7227");

        List<BlastProtein> filteredResults = filter.getMatchingEntries();

        Assert.assertEquals(false, filteredResults.isEmpty());

        for (BlastProtein prot : filteredResults){
            System.out.println(prot.getAccession());
            System.out.println(prot.getIdentity());
        }
        Assert.assertEquals(3, filteredResults.size());
    }

    @Test
    public void getResult_FilterOrganismAndIdentity(){

        filter.filterResultsWithIdentityAndOrganism((float)100, "7227");

        List<BlastProtein> filteredResults = filter.getMatchingEntries();

        Assert.assertEquals(false, filteredResults.isEmpty());

        for (BlastProtein prot : filteredResults){
            System.out.println(prot.getAccession());
            System.out.println(prot.getIdentity());
        }
        Assert.assertEquals(3, filteredResults.size());
    }

    @Test
    public void getResult_FilterOnMatchingEntries(){

        filter.readResultsWithoutFiltering();

        List<BlastProtein> filteredResultsOnIdentity = filter.filterMappingEntriesWithIdentity((float)100);

        Assert.assertEquals(false, filteredResultsOnIdentity.isEmpty());
        Assert.assertEquals(41, filteredResultsOnIdentity.size());

        List<BlastProtein> filteredResultsOnOrganism = filter.filterMappingEntriesWithOrganism("7227");

        Assert.assertEquals(false, filteredResultsOnOrganism.isEmpty());
        Assert.assertEquals(3, filteredResultsOnOrganism.size());

        List<BlastProtein> filteredResultsOnOrganismAndIdentity = filter.filterMappingEntriesWithIdentityAndOrganism((float)100, "7227");

        Assert.assertEquals(false, filteredResultsOnOrganismAndIdentity.isEmpty());
        Assert.assertEquals(3, filteredResultsOnOrganismAndIdentity.size());

        List<BlastProtein> filteredResultsWithTotalAlignment = BlastResultFilter.collectMappingEntriesWithTotalAlignment(filteredResultsOnIdentity, this.sequence.length());
        Assert.assertEquals(28, filteredResultsWithTotalAlignment.size());
    }
}