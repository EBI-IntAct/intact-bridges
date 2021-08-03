package uk.ac.ebi.intact.uniprot.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.uniprot.UniprotServiceException;
import uk.ac.ebi.intact.uniprot.data.MockUniProtEntries;
import uk.ac.ebi.intact.uniprot.model.*;
import uk.ac.ebi.intact.uniprot.service.referenceFilter.CrossReferenceFilter;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * UniprotRemoteServiceAdapter Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class UniprotRemoteServiceTest {

    public static final Log log = LogFactory.getLog(UniprotRemoteServiceTest.class);

    /////////////////////
    // Utility

    private UniprotService getUniprotService() {
        return new UniprotRemoteService();
    }

    private UniprotService getUniprotService(CrossReferenceFilter filter) {
        if (filter == null) {
            fail("you must give a non null filter !!");
        }
        AbstractUniprotService service = (AbstractUniprotService) getUniprotService();
        service.setCrossReferenceSelector(filter);
        return service;
    }

    ////////////////////
    // Tests
    @Test
    public void convert_CDC42_CANFA() throws Exception {
        UniProtEntry entry = MockUniProtEntries.build_P60952();
        UniprotRemoteService service = new UniprotRemoteService();
        UniprotProtein protein = service.buildUniprotProtein(entry, true);

        assertEquals(2, protein.getSpliceVariants().size());

        UniprotSpliceVariant sv1 = searchSpliceVariantByIsoId("P60952-1", protein);
        assertNotNull(sv1);
        assertTrue(sv1.getSecondaryAcs().contains("P21181-1"));
        assertEquals("Has not been isolated in dog so far ", sv1.getNote());
        assertEquals(1, sv1.getSynomyms().size());
        assertEquals("Brain", sv1.getSynomyms().iterator().next());
        assertEquals(protein.getSequence(), sv1.getSequence());

        UniprotSpliceVariant sv2 = searchSpliceVariantByIsoId("P60952-2", protein);
        assertNotNull(sv2);
        assertTrue(sv2.getSecondaryAcs().contains("P21181-4"));
        assertEquals(" ", sv2.getNote());
        assertEquals(1, sv2.getSynomyms().size());
        assertEquals("Placental", sv2.getSynomyms().iterator().next());
    }

    @Test
    public void retrieveByUniprotId() throws Exception {
        Collection<UniprotProtein> prots = getUniprotService().retrieve("CDK1_HUMAN");
        final UniprotProtein uniprotProtein = prots.iterator().next();
        assertEquals("P06493", uniprotProtein.getPrimaryAc());
        assertEquals("Cyclin-dependent kinase 1", uniprotProtein.getDescription());
        assertEquals(7, uniprotProtein.getSynomyms().size());

        Assert.assertEquals("Homo sapiens", uniprotProtein.getOrganism().getName());
        Assert.assertEquals("Human", uniprotProtein.getOrganism().getCommonName());
    }

    @Test
    public void retrieveByUniprotId_noGeneNames() throws Exception {
        Collection<UniprotProtein> prots = getUniprotService().retrieve("O58917");

        UniprotProtein uniprotProtein = prots.iterator().next();
        assertTrue(uniprotProtein.getGenes().isEmpty());
    }

    @Test
    public void retrieveByUniprotId_chainWithStrangeSequenceBoundaries() throws Exception {
        // This protein contains a chain PRO_0000296200, with boundaries 1-?

        Collection<UniprotProtein> prots = getUniprotService().retrieve("P18850");
        final UniprotProtein uniprotProtein = prots.iterator().next();
        assertEquals("P18850", uniprotProtein.getPrimaryAc());
    }

    @Test
    public void retrieveBySpliceVariantId() throws Exception {
        Collection<UniprotProtein> prots = getUniprotService().retrieve("Q13535-1");
        assertEquals("ATR_HUMAN", prots.iterator().next().getId());
    }

    @Test
    public void retrieveSpliceVariant() throws Exception {
        Collection<UniprotSpliceVariant> prots = getUniprotService().retrieveSpliceVariant("Q13535-1");
        UniprotSpliceVariant spliceVariant = prots.iterator().next();

        assertEquals("Q13535-1", spliceVariant.getPrimaryAc());
        assertEquals("Q13535", spliceVariant.getMasterProtein().getPrimaryAc());
    }

    @Test
    public void retrieveFeatureChain() throws Exception {
        Collection<UniprotFeatureChain> prots = getUniprotService().retrieveFeatureChain("P97887-PRO_0000025599");
        assertEquals("Presenilin-1 NTF subunit", prots.iterator().next().getDescription());
    }

    @Test
    public void retrieveFeaturePeptide() throws Exception {
        Collection<UniprotFeatureChain> prots = getUniprotService().retrieveFeatureChain("P01019-PRO_0000032459");
        Assert.assertFalse(prots.isEmpty());
        assertEquals("Angiotensin-3", prots.iterator().next().getDescription());
    }

    @Test
    public void convert_FAU_DROME() throws Exception {

        UniProtEntry entry = MockUniProtEntries.build_Q9VGX3();
        UniprotRemoteService service = new UniprotRemoteService();
        UniprotProtein protein = service.buildUniprotProtein(entry, true);

        assertTrue(entry.getProteinDescription().hasRecommendedName());

        assertEquals(UniprotProteinType.SWISSPROT, protein.getSource());

        assertEquals("FAU_DROME", protein.getId());

        assertEquals("Q9VGX3", protein.getPrimaryAc());

        assertEquals(4, protein.getSecondaryAcs().size());
        assertTrue(protein.getSecondaryAcs().contains("Q95S18"));
        assertTrue(protein.getSecondaryAcs().contains("Q9VGX1"));
        assertTrue(protein.getSecondaryAcs().contains("Q9VGX2"));
        assertTrue(protein.getSecondaryAcs().contains("Q9Y0F9"));

        assertEquals("Protein anoxia up-regulated", protein.getDescription());

        assertEquals(1, protein.getGenes().size());
        assertTrue(protein.getGenes().contains("fau"));

        assertEquals(0, protein.getSynomyms().size());

        assertEquals(1, protein.getOrfs().size());
        assertTrue(protein.getOrfs().contains("CG6544"));

        assertEquals(0, protein.getLocuses().size());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        assertEquals(sdf.parse("23-JAN-2007"), protein.getLastAnnotationUpdate());
        assertEquals(sdf.parse("19-JUL-2004"), protein.getLastSequenceUpdate());
        assertEquals("SP_41", protein.getReleaseVersion());

        assertEquals(0, protein.getKeywords().size());

        assertEquals(0, protein.getDiseases().size());

        assertEquals(7227, protein.getOrganism().getTaxid());
        assertEquals("Drosophila melanogaster", protein.getOrganism().getName());

        assertEquals("7DDCB26AD1AB9CEE", protein.getCrc64());
        assertEquals("MVYESGFTTRRTYSSRPVTTSYAVTYPSVEKVTRVYKSSYPIYSSYSVPRRVYGATRVVT" +
                "SPIRVVTSPARVVSRVIHSPSPVRVVRTTTRVISSPERTTYSYTTPSTYYSPSYLPSTYT" +
                "STYIPTSYTTYTPSYAYSPTTVTRVYAPRSSLSPLRITPSPVRVITSPVRSVPSYLKRLP" +
                "PGYGARALTNYLNTEPFTTFSEETSRIRNRAQSLIRDLHTPVVRRARSCTPFPVTGYTYE" +
                "PASQLALDAYVARVTNPVRHIAKEVHNISHYPRPAVKYVDAELDPNRPSRKFSAPRPLED" +
                "PLDVEAKEKQRLRQERLLTVNEEALDEVDLEKKRAQKADEAKRREERALKEERDRLTAEA" +
                "EKQAAAKAKKAAEEAAKIAAEEALLAEAAAQKAAEEAKALKAAEDAAQKAAEEARLAEEA" +
                "AAQKVAEEAAQKAAEEARLAEEAAAQKAAEEAAQKAAEEAALKAAEEARLAEEAAQKAAE" +
                "EAALKAVEEARAAEEAAQKAAEEARVAEEARLEEEQRVREQELERLAEIEKESEGELARQ" +
                "AAELAEIARQESELAAQELQAIQKNENETSEPVVEEPVTPVEEQEPIIELGSNVTPTGGN" +
                "SYEEDLDAEEEEDEEEEEE", protein.getSequence());
        assertEquals(protein.getSequence().length(), protein.getSequenceLength());

        assertEquals(2, protein.getSpliceVariants().size());

        UniprotSpliceVariant sv1 = searchSpliceVariantByIsoId("Q9VGX3-5", protein);
        assertNotNull(sv1);
        assertEquals(protein.getSequence(), sv1.getSequence());

        UniprotSpliceVariant sv2 = searchSpliceVariantByIsoId("Q9VGX3-2", protein);
        assertNotNull(sv2);
        assertEquals("MVYESGFTTRRTYSSRPVTTSYAVTYPSVEKVTRVYKSSYPIYSSYSVPRRVYGA" +
                        "TRVVTSPIRVVTSPARVVSRVIHSPSPVRVVRTTTRVISSPERTTYSYTTPSTYYSPSYLP" +
                        "STYTSTYIPTSYTTYTPSYAYSPTTVTRVYAPRSSLSPLRITPSPVRVITSPVRSVPSYLK" +
                        "RLPPGYGARALTNYLNTEPFTTFSEETSRIRNRAQSLIRDLHTPVVRRARSCTPFPVTGYT" +
                        "YEPASQLALDAYVARVTNPVRHIAKEVHNISHYPRPAVKYVDAELDPNRPSRKFSAPRPLE" +
                        "DPLDVEAKEKQRLRQERLLTVNEEALDEVDLEKKRAQKADEAKRREERALKEERDRLTAEA" +
                        "EKQAAAKAKKAAEEAAKIAAEEALLAEAAAQKAAEEAKALKAAEDAAQKAAEEARLAEEAA" +
                        "AQKVAEEAAQKAAEEARLAEEAAAQKAAEEAAQKAAEEAALKAAEEARLAEEAAQKAAEEA" +
                        "ALKAVEEARAAEEAAQKAAEEARVAEEARLEEEQRVREQELERLAEIEKESEGELARQAAE" +
                        "LAEIARQESELAAQELQAIQKNENETSEPVVEEPVTPVEEQEPIIELGSNVTPTGGNSYEE" +
                        "DLDAEEEEDEEEEEE",
                sv2.getSequence());

        assertEquals(0, protein.getFeatureChains().size());
    }

    private UniprotSpliceVariant searchSpliceVariantByIsoId(String isoId, UniprotProtein protein) {

        for (UniprotSpliceVariant usv : protein.getSpliceVariants()) {
            if (usv.getPrimaryAc().equals(isoId)) {
                return usv;
            }
        }
        return null;
    }

    @Test
    public void build() throws Exception {
        UniprotService uniprot = getUniprotService();

        Collection<UniprotProtein> proteins = uniprot.retrieve("P47068");

        assertNotNull(proteins);
        assertEquals(1, proteins.size());
        UniprotProtein protein = proteins.iterator().next();

        assertEquals("BBC1_YEAST", protein.getId());
        assertEquals("Myosin tail region-interacting protein MTI1", protein.getDescription());

        assertEquals("P47068", protein.getPrimaryAc());
        assertEquals("D6VWG0", protein.getSecondaryAcs().get(0));
        assertEquals("P47067", protein.getSecondaryAcs().get(1));

        // gene names
        assertEquals(1, protein.getGenes().size());
        assertEquals("BBC1", protein.getGenes().iterator().next());

        assertEquals(2, protein.getSynomyms().size());
        assertEquals("MTI1", protein.getSynomyms().iterator().next());

        assertEquals(3, protein.getOrfs().size());
        assertEquals("J1286", protein.getOrfs().iterator().next());

        assertEquals(1, protein.getLocuses().size());
        assertTrue(protein.getLocuses().contains("YJL020C"));

        // sequence
        String sequence = "MSEPEVPFKVVAQFPYKSDYEDDLNFEKDQEIIVTSVEDAEWYFGEYQDSNGDVIEGIFP" +
                "KSFVAVQGSEVGKEAESSPNTGSTEQRTIQPEVEQKDLPEPISPETKKETLSGPVPVPAA" +
                "TVPVPAATVPVPAATAVSAQVQHDSSSGNGERKVPMDSPKLKARLSMFNQDITEQVPLPK" +
                "STHLDLENIPVKKTIVADAPKYYVPPGIPTNDTSNLERKKSLKENEKKIVPEPINRAQVE" +
                "SGRIETENDQLKKDLPQMSLKERIALLQEQQRLQAAREEELLRKKAKLEQEHERSAVNKN" +
                "EPYTETEEAEENEKTEPKPEFTPETEHNEEPQMELLAHKEITKTSREADEGTNDIEKEQF" +
                "LDEYTKENQKVEESQADEARGENVAEESEIGYGHEDREGDNDEEKEEEDSEENRRAALRE" +
                "RMAKLSGASRFGAPVGFNPFGMASGVGNKPSEEPKKKQHKEKEEEEPEQLQELPRAIPVM" +
                "PFVDPSSNPFFRKSNLSEKNQPTETKTLDPHATTEHEQKQEHGTHAYHNLAAVDNAHPEY" +
                "SDHDSDEDTDDHEFEDANDGLRKHSMVEQAFQIGNNESENVNSGEKIYPQEPPISHRTAE" +
                "VSHDIENSSQNTTGNVLPVSSPQTRVARNGSINSLTKSISGENRRKSINEYHDTVSTNSS" +
                "ALTETAQDISMAAPAAPVLSKVSHPEDKVPPHPVPSAPSAPPVPSAPSVPSAPPVPPAPP" +
                "ALSAPSVPPVPPVPPVSSAPPALSAPSIPPVPPTPPAPPAPPAPLALPKHNEVEEHVKSS" +
                "APLPPVSEEYHPMPNTAPPLPRAPPVPPATFEFDSEPTATHSHTAPSPPPHQNVTASTPS" +
                "MMSTQQRVPTSVLSGAEKESRTLPPHVPSLTNRPVDSFHESDTTPKVASIRRSTTHDVGE" +
                "ISNNVKIEFNAQERWWINKSAPPAISNLKLNFLMEIDDHFISKRLHQKWVVRDFYFLFEN" +
                "YSQLRFSLTFNSTSPEKTVTTLQERFPSPVETQSARILDEYAQRFNAKVVEKSHSLINSH" +
                "IGAKNFVSQIVSEFKDEVIQPIGARTFGATILSYKPEEGIEQLMKSLQKIKPGDILVIRK" +
                "AKFEAHKKIGKNEIINVGMDSAAPYSSVVTDYDFTKNKFRVIENHEGKIIQNSYKLSHMK" +
                "SGKLKVFRIVARGYVGW";
        assertEquals(1157, protein.getSequenceLength());
        assertEquals(1157, protein.getSequence().length());
        assertEquals(sequence, protein.getSequence());

        // DT lines
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(protein.getLastSequenceUpdate());
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            assertEquals(formatter.parse("17-JAN-2003"), calendar.getTime());

            formatter = null;
        } catch (ParseException e) {
            fail("Date parsing should not fail here.");
        }

        // functions
        assertEquals(1, protein.getFunctions().size());
        assertEquals("Involved in the regulation of actin cytoskeleton", protein.getFunctions().iterator().next());

        // keywords
        assertEquals(9, protein.getKeywords().size());

        // cross references
        assertEquals(24, protein.getCrossReferences().size());

        // splice variants
        assertEquals(0, protein.getSpliceVariants().size());

        // feature chain
        assertEquals(1, protein.getFeatureChains().size());
    }

    @Test
    public void searchBySpliceVariant() throws UniprotServiceException {

        // Q8NG31-1 has parent Q8NG31
        UniprotService uniprot = getUniprotService();

        Collection<UniprotProtein> proteins = uniprot.retrieve("Q8NG31-1");

        assertEquals(1, proteins.size());
        UniprotProtein protein = proteins.iterator().next();
        // search for splice variant Q8NG31-1 in the protein
        boolean found = false;
        for (UniprotSpliceVariant sv : protein.getSpliceVariants()) {
            if ("Q8NG31-1".equals(sv.getPrimaryAc())) {
                found = true;
                break; // exits the loop
            }
        }

        if (!found) {
            fail("Could not find Splice Variant: Q8NG31-1");
        }
    }

    @Test
    public void searchBySpliceVariantSecondaryId() throws UniprotServiceException {

        // Q8NG31-1 has parent Q8NG31
        UniprotService uniprot = getUniprotService();

        // note: this splice variant is showing up in CDC42_CANFA, CDC42_HUMAN and CDC42_MOUSE
        Collection<UniprotProtein> proteins = uniprot.retrieve("P21181-1");

        assertEquals(3, proteins.size());
        for (UniprotProtein protein : proteins) {

            // search for splice variant P21181-1 in the current protein
            boolean found = false;
            for (UniprotSpliceVariant sv : protein.getSpliceVariants()) {
                if (sv.getSecondaryAcs().contains("P21181-1")) {
                    found = true;
                    log.debug("Found Splice Variant P21181-1 (secondary ac) in protein " + protein.getId() + ".");
                    break; // exits the splice variant loop
                }
            }

            if (!found) {
                fail("Could not find Splice Variant P21181-1 (secondary ac) in protein " + protein.getId() + ".");
            }
        }
    }

    @Test
    public void retrieveProteinWithSpliceVariant() throws UniprotServiceException {

        UniprotService uniprot = getUniprotService();
        Collection<UniprotProtein> proteins = uniprot.retrieve("Q24208");

        assertNotNull(proteins);
        assertEquals(1, proteins.size());

        UniprotProtein protein = proteins.iterator().next();
        assertNotNull(protein.getSpliceVariants());
        assertEquals(2, protein.getSpliceVariants().size());

        boolean sv1 = false;
        boolean sv3 = false;

        log.debug("");
        log.debug("parent: " + protein.getSequence());
        log.debug("parent's length: " + protein.getSequence().length());

        for (UniprotSpliceVariant sv : protein.getSpliceVariants()) {

            assertEquals(sv.getPrimaryAc(), protein.getOrganism(), sv.getOrganism());

            if ("Q24208-1".equals(sv.getPrimaryAc())) {

                assertEquals(0, sv.getSecondaryAcs().size());
                assertEquals("eIF-2gamma", sv.getSynomyms().iterator().next());
                assertEquals("MATAEAQIGVNRNLQKQDLSNLDVSKLTPLSPEVISRQATINIGTIGHVAHGKSTVVKAI" +
                        "SGVQTVRFKNELERNITIKLGYANAKIYKCDNPKCPRPASFVSDASSKDDSLPCTRLNCS" +
                        "GNFRLVRHVSFVDCPGHDILMATMLNGAAVMDAALLLIAGNESCPQPQTSEHLAAIEIMK" +
                        "LKQILILQNKIDLIKESQAKEQYEEITKFVQGTVAEGAPIIPISAQLKYNIDVLCEYIVN" +
                        "KIPVPPRDFNAPPRLIVIRSFDVNKPGCEVADLKGGVAGGSILSGVLKVGQEIEVRPGVV" +
                        "TKDSDGNITCRPIFSRIVSLFAEQNELQYAVPGGLIGVGTKIDPTLCRADRLVGQVLGAV" +
                        "GQLPDIYQELEISYYLLRRLLGVRTDGDKKGARVEKLQKNEILLVNIGSLSTGGRISATK" +
                        "GDLAKIVLTTPVCTEKGEKIALSRRVENHWRLIGWGQIFGGKTITPVLDSQVAKK", sv.getSequence());
                sv1 = true;

            } else if ("Q24208-2".equals(sv.getPrimaryAc())) {

                assertEquals(0, sv.getSecondaryAcs().size());
                assertEquals(0, sv.getSynomyms().size());
                assertEquals(null, sv.getStart());
                assertEquals(null, sv.getEnd());

                String s = "MHLRGDVLLGGVAADVSKLTPLSPEVISRQATINIGTIGHVAHGKSTVVKAISGVQTVRF" +
                        "KNELERNITIKLGYANAKIYKCDNPKCPRPASFVSDASSKDDSLPCTRLNCSGNFRLVRH" +
                        "VSFVDCPGHDILMATMLNGAAVMDAALLLIAGNESCPQPQTSEHLAAIEIMKLKQILILQ" +
                        "NKIDLIKESQAKEQYEEITKFVQGTVAEGAPIIPISAQLKYNIDVLCEYIVNKIPVPPRD" +
                        "FNAPPRLIVIRSFDVNKPGCEVADLKGGVAGGSILSGVLKVGQEIEVRPGVVTKDSDGNI" +
                        "TCRPIFSRIVSLFAEQNELQYAVPGGLIGVGTKIDPTLCRADRLVGQVLGAVGQLPDIYQ" +
                        "ELEISYYLLRRLLGVRTDGDKKGARVEKLQKNEILLVNIGSLSTGGRISATKGDLAKIVL" +
                        "TTPVCTEKGEKIALSRRVENHWRLIGWGQIFGGKTITPVLDSQVAKK";

                log.debug(s.length());
                log.debug(s + "\n");
                log.debug(sv.getSequence());
                log.debug(sv.getSequence().length());

                assertEquals("MHLRGDVLLGGVAADVSKLTPLSPEVISRQATINIGTIGHVAHGKSTVVKAISGVQT" +
                        "VRFKNELERNITIKLGYANAKIYKCDNPKCPRPASFVSDASSKDDSLPCTRLNCSGN" +
                        "FRLVRHVSFVDCPGHDILMATMLNGAAVMDAALLLIAGNESCPQPQTSEHLAAIEIM" +
                        "KLKQILILQNKIDLIKESQAKEQYEEITKFVQGTVAEGAPIIPISAQLKYNIDVLCE" +
                        "YIVNKIPVPPRDFNAPPRLIVIRSFDVNKPGCEVADLKGGVAGGSILSGVLKVGQEI" +
                        "EVRPGVVTKDSDGNITCRPIFSRIVSLFAEQNELQYAVPGGLIGVGTKIDPTLCRAD" +
                        "RLVGQVLGAVGQLPDIYQELEISYYLLRRLLGVRTDGDKKGARVEKLQKNEILLVNI" +
                        "GSLSTGGRISATKGDLAKIVLTTPVCTEKGEKIALSRRVENHWRLIGWGQIFGGKTI" +
                        "TPVLDSQVAKK", sv.getSequence());
                sv3 = true;

            } else {
                fail("Unknown splice variant: " + sv.getPrimaryAc());
            }
        } // for

        assertTrue("Q24208-1 was missing from the splice variant list.", sv1);
        assertTrue("Q24208-2 was missing from the splice variant list.", sv3);
    }

    @Test
    public void retrieveProteinWithChain() throws UniprotServiceException {

        UniprotService uniprot = getUniprotService();
        Collection<UniprotProtein> proteins = uniprot.retrieve("P18459");

        assertNotNull(proteins);
        assertEquals(1, proteins.size());

        UniprotProtein protein = proteins.iterator().next();
        assertNotNull(protein.getFeatureChains());
        assertEquals(1, protein.getFeatureChains().size());

        final UniprotFeatureChain chain = protein.getFeatureChains().iterator().next();

        Assert.assertEquals("P18459-PRO_0000205566", chain.getPrimaryAc());

        Assert.assertEquals(7227, chain.getOrganism().getTaxid());

        Assert.assertEquals("Tyrosine 3-monooxygenase", chain.getDescription());

        Assert.assertEquals(1, (int) chain.getStart());
        Assert.assertEquals(579, (int) chain.getEnd());

        Assert.assertEquals("MMAVAAAQKNREMFAIKKSYSIENGYPSRRRSLVDDARFETLVVKQTKQTVLEEARSKAN" +
                "DDSLEDCIVQAQEHIPSEQDVELQDEHANLENLPLEEYVPVEEDVEFESVEQEQSESQSQ" +
                "EPEGNQQPTKNDYGLTEDEILLANAASESSDAEAAMQSAALVVRLKEGISSLGRILKAIE" +
                "TFHGTVQHVESRQSRVEGVDHDVLIKLDMTRGNLLQLIRSLRQSGSFSSMNLMADNNLNV" +
                "KAPWFPKHASELDNCNHLMTKYEPDLDMNHPGFADKVYRQRRKEIAEIAFAYKYGDPIPF" +
                "IDYSDVEVKTWRSVFKTVQDLAPKHACAEYRAAFQKLQDEQIFVETRLPQLQEMSDFLRK" +
                "NTGFSLRPAAGLLTARDFLASLAFRIFQSTQYVRHVNSPYHTPEPDSIHELLGHMPLLAD" +
                "PSFAQFSQEIGLASLGASDEEIEKLSTVYWFTVEFGLCKEHGQIKAYGAGLLSSYGELLH" +
                "AISDKCEHRAFEPASTAVQPYQDQEYQPIYYVAESFEDAKDKFRRWVSTMSRPFEVRFNP" +
                "HTERVEVLDSVDKLETLVHQMNTEILHLTNAISKLRRPF", chain.getSequence());
    }

//TODO: Needs to be checked!!
//    @Test
//    public void retrieveMultipleProteins() throws UniprotServiceException {
//        UniprotService uniprot = getUniprotService();
//        Collection<UniprotProtein> proteins = uniprot.retrieve( "P21181" );
//
//        assertNotNull( proteins );
//        assertEquals( 3, proteins.size() );
//
//        Collection<String> ids = new ArrayList<String>();
//        ids.add( "CDC42_CANFA" );
//        ids.add( "CDC42_HUMAN" );
//        ids.add( "CDC42_MOUSE" );
//
//        for ( UniprotProtein protein : proteins ) {
//            assertTrue( ids.contains( protein.getId() ) );
//        }
//    }

    @Test
    public void retrieveEntryWithExternalSpliceVar() throws UniprotServiceException {
        UniprotService uniprotService = getUniprotService();
        Collection<UniprotProtein> proteins = uniprotService.retrieve("P47100");

        UniprotProtein prot = proteins.iterator().next();

        List<UniprotSpliceVariant> uniprotSpliceVariants = new ArrayList<UniprotSpliceVariant>(prot.getSpliceVariants());

        Assert.assertEquals(2, uniprotSpliceVariants.size());
        Assert.assertEquals("P47100-1", uniprotSpliceVariants.get(0).getPrimaryAc());
        Assert.assertTrue(uniprotSpliceVariants.get(0).getSequence() != null);
        Assert.assertEquals("P47099-1", uniprotSpliceVariants.get(1).getPrimaryAc());
        Assert.assertTrue(uniprotSpliceVariants.get(1).getSequence() != null);
    }

    @Test
    public void retrieveEntryWithExternalSpliceVar2() throws UniprotServiceException {
        UniprotService uniprotService = getUniprotService();
        Collection<UniprotProtein> proteins = uniprotService.retrieve("Q03001");

        UniprotProtein prot = proteins.iterator().next();

        List<UniprotSpliceVariant> uniprotSpliceVariants = new ArrayList<UniprotSpliceVariant>(prot.getSpliceVariants());

        Assert.assertEquals(9, uniprotSpliceVariants.size());

        for (UniprotSpliceVariant usv : uniprotSpliceVariants) {
            Assert.assertNotNull("Sequence for " + usv.getPrimaryAc() + " must not be null", usv.getSequence());
        }
    }

    @Test
    public void retrieveSimpleProteinWithCrossReferenceFilter() {

        UniprotService uniprot = getUniprotService(new DefaultCrossReferenceFilter());
        Collection<UniprotProtein> proteins = uniprot.retrieve("P47068");

        assertNotNull(proteins);
        assertEquals(1, proteins.size());
        UniprotProtein protein = proteins.iterator().next();

        assertEquals(UniprotProteinType.SWISSPROT, protein.getSource());

        // check that we have not so many cross references
        // cross references
        assertEquals(24, protein.getCrossReferences().size());

        assertTrue(protein.getCrossReferences().contains(new UniprotXref("NP_012514.2", "RefSeq", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("1TG0", "PDB", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("1WDX", "PDB", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("1ZUK", "PDB", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("2DYF", "PDB", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("DIP-6279N", "DIP", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("P47068", "MINT", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("YJL020C_mRNA", "EnsemblFungi", "transcript")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("YJL020C", "EnsemblFungi", "protein")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("YJL020C", "EnsemblFungi", "gene")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("S000003557", "SGD", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0030479", "GO", "actin cortical patch")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0051015", "GO", "actin filament binding")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0017024", "GO", "myosin I binding")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0035091", "GO", "phosphatidylinositol binding")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0051666", "GO", "actin cortical patch localization")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0030036", "GO", "actin cytoskeleton organization")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0051017", "GO", "actin filament bundle assembly")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0007010", "GO", "cytoskeleton organization")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("GO:0034316", "GO", "negative regulation of Arp2/3 complex-mediated actin nucleation")));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("IPR030506", "InterPro", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("IPR035552", "InterPro", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("IPR036028", "InterPro", null)));
        assertTrue(protein.getCrossReferences().contains(new UniprotXref("IPR001452", "InterPro", null)));

    }

    @Test
    public void validateXrefsSimpleProteinWithSpliceVariants() throws Exception {

        UniprotService uniprot = getUniprotService(new DefaultCrossReferenceFilter());
        Collection<UniprotProtein> proteins = uniprot.retrieve("P04637");

        assertNotNull(proteins);
        assertEquals(1, proteins.size());
        UniprotProtein protein = proteins.iterator().next();

        assertEquals(UniprotProteinType.SWISSPROT, protein.getSource());

        // check that we have not so many cross references
        // cross references
        assertEquals(468, protein.getCrossReferences().size());
        assertEquals(5, protein.getCrossReferences().stream().filter(xref -> xref.getDatabase().equals("Ensembl")).count());
        assertEquals(221, protein.getCrossReferences().stream().filter(xref -> xref.getDatabase().equals("PDB")).count());
        assertEquals(178, protein.getCrossReferences().stream().filter(xref -> xref.getDatabase().equals("GO")).count());
        assertEquals(15, protein.getCrossReferences().stream().filter(xref -> xref.getDatabase().equals("RefSeq")).count());
        assertEquals(39, protein.getCrossReferences().stream().filter(xref -> xref.getDatabase().equals("Reactome")).count());
        assertEquals(8, protein.getCrossReferences().stream().filter(xref -> xref.getDatabase().equals("InterPro")).count());
        assertEquals(1, protein.getCrossReferences().stream().filter(xref -> xref.getDatabase().equals("DIP")).count());


        /*
            ENST00000269305; ENSP00000269305; ENSG00000141510 [P04637-1]
            ENST00000445888; ENSP00000391478; ENSG00000141510 [P04637-1]
            ENST00000420246; ENSP00000391127; ENSG00000141510 [P04637-2]
            ENST00000455263; ENSP00000398846; ENSG00000141510 [P04637-3]
            ENST00000610292; ENSP00000478219; ENSG00000141510 [P04637-4]
            ENST00000619485; ENSP00000482537; ENSG00000141510 [P04637-4]
            ENST00000620739; ENSP00000481638; ENSG00000141510 [P04637-4]
            ENST00000622645; ENSP00000482222; ENSG00000141510 [P04637-5]
            ENST00000610538; ENSP00000480868; ENSG00000141510 [P04637-6]
            ENST00000504937; ENSP00000481179; ENSG00000141510 [P04637-7]
            ENST00000510385; ENSP00000478499; ENSG00000141510 [P04637-8]
            ENST00000504290; ENSP00000484409; ENSG00000141510 [P04637-9]
         */
        assertEquals(9, protein.getSpliceVariants().size());
        for (UniprotSpliceVariant spliceVariant : protein.getSpliceVariants()) {
            switch (spliceVariant.getPrimaryAc()) {
                case "P04637-1":
                    assertTrue(spliceVariant.isCanonical());
                    assertEquals(5, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-1")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000269305", "Ensembl", "transcript", "P04637-1")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000445888", "Ensembl", "transcript", "P04637-1")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000391478", "Ensembl", "protein", "P04637-1")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000391478", "Ensembl", "protein", "P04637-1")));
                    break;
                case "P04637-2":
                    assertEquals(3, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-2")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000420246", "Ensembl", "transcript", "P04637-2")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000391127", "Ensembl", "protein", "P04637-2")));
                    break;
                case "P04637-3":
                    assertEquals(3, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-3")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000455263", "Ensembl", "transcript", "P04637-3")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000398846", "Ensembl", "protein", "P04637-3")));
                    break;
                case "P04637-4":
                    assertEquals(7, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-4")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000610292", "Ensembl", "transcript", "P04637-4")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000619485", "Ensembl", "transcript", "P04637-4")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000620739", "Ensembl", "transcript", "P04637-4")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000478219", "Ensembl", "protein", "P04637-4")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000482537", "Ensembl", "protein", "P04637-4")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000481638", "Ensembl", "protein", "P04637-4")));
                    break;
                case "P04637-5":
                    assertEquals(3, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-5")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000622645", "Ensembl", "transcript", "P04637-5")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000482222", "Ensembl", "protein", "P04637-5")));
                    break;
                case "P04637-6":
                    assertEquals(3, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-6")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000610538", "Ensembl", "transcript", "P04637-6")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000480868", "Ensembl", "protein", "P04637-6")));
                    break;
                case "P04637-7":
                    assertEquals(3, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-7")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000504937", "Ensembl", "transcript", "P04637-7")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000481179", "Ensembl", "protein", "P04637-7")));
                    break;
                case "P04637-8":
                    assertEquals(3, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-8")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000510385", "Ensembl", "transcript", "P04637-8")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000478499", "Ensembl", "protein", "P04637-8")));
                    break;
                case "P04637-9":
                    assertEquals(3, spliceVariant.getCrossReferences().size());
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSG00000141510", "Ensembl", "gene", "P04637-9")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENST00000504290", "Ensembl", "transcript", "P04637-9")));
                    assertTrue(spliceVariant.getCrossReferences().contains(new UniprotXref("ENSP00000484409", "Ensembl", "protein", "P04637-9")));
                    break;
                default:
                    //Thrown error
                    throw new Exception("Invalid isoform for P04637");
            }
        }
        ;
    }

    @Test
    public void xxx() throws Exception {

        UniprotService uniprot = getUniprotService();
        Collection<UniprotProtein> proteins = uniprot.retrieve("Q0G819");

    }
}