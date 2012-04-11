package uk.ac.ebi.intact.bridges.imexcentral.mock;

import edu.ucla.mbi.imex.central.ws.v20.*;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.bridges.imexcentral.Operation;
import uk.ac.ebi.intact.bridges.imexcentral.PublicationStatus;

import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Dummy service that one can use to write test against.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.2
 */
public class MockImexCentralClient implements ImexCentralClient {

    public MockImexCentralClient() {
        allPublications = new ArrayList<Publication>( );
    }

    private int imexIdSequence = 1;
    List<Publication> allPublications;
    private static Pattern PUBMED_REGEXP = Pattern.compile("\\d+");
    
    private String INTACT_GROUP = "INTACT";
    private String MATRIXDB_GROUP = "MATRIXDB";
    private String intact_user = "intact";
    private String phantom_user = "phantom";

    private static Pattern pubmed_regexp = Pattern.compile("\\d+");

    /////////////////////////////
    // Service initialization

    public void initImexSequence( int sequence ) {
        if( sequence < 1 ) {
            throw new IllegalArgumentException( "You must give a positive sequence: " + sequence );
        }
        imexIdSequence = sequence;
    }

    public void initPublications( List<Publication> allPublications ) {
        if ( allPublications == null ) {
            throw new IllegalArgumentException( "You must give a non null allPublications" );
        }
        this.allPublications = allPublications;
    }

    public int getNextSequenceValue() {
        return imexIdSequence;
    }

    public void addPublication( Publication p ) {
        if ( p == null ) {
            throw new IllegalArgumentException( "You must give a non null publication" );
        }
        allPublications.add( p );
    }

    public void addPublication( String identifier, String imexAccession, String status, String owner ) {
        Publication p = new Publication();
        final Identifier id = new Identifier();
        id.setAc( identifier );
        p.getIdentifier().add(id);
        p.setImexAccession( ( imexAccession == null ? "N/A" : imexAccession ) );
        p.setStatus( status );
        p.setOwner( owner );
        allPublications.add( p );
    }

    ///////////////////////
    // ImexCentralClient

    public String getEndpoint() {
        return null;
    }

    public Publication getPublicationById( String identifier ) throws ImexCentralException {

        for ( Publication p : allPublications ) {
           for (Identifier i : p.getIdentifier()){
               if( identifier.equals( i.getAc() ) ) {
                   return p;
               }
           } 
            
            if (p.getImexAccession() != null && p.getImexAccession().equalsIgnoreCase(identifier)){
                return p;
            }
        }

        // no publication record found, throw an exception (same behaviour as webservice)
        ImexCentralFault imexFault = new ImexCentralFault();
        imexFault.setFaultCode(6);

        IcentralFault fault = new IcentralFault("identifier not found", imexFault);
        throw new ImexCentralException(fault);
    }

    public List<Publication> getPublicationByOwner( String owner, int first, int max, Holder<PublicationList> pubList, Holder<Long> number ) throws ImexCentralException {
        List<Publication> publications = new ArrayList<Publication>( );
        for ( int i = first; i <= max; i++ ) {
            Publication p = allPublications.get(i);
           if( owner.equals(p.getOwner()) ) {
               publications.add( p );
           }
        }
        return publications;
    }

    public List<Publication> getPublicationByStatus( String status, int first, int max, Holder<PublicationList> pubList, Holder<Long> number ) {
        List<Publication> publications = new ArrayList<Publication>( );
        for ( int j = first; j <= max; j++ ) {
            Publication p = allPublications.get(j);
            
            if (p.getStatus().equalsIgnoreCase(status)){
                publications.add( p );
            }
        }
        return publications;
    }

    public Publication updatePublicationStatus( String identifier, PublicationStatus status, String message ) throws ImexCentralException {
        final Publication p = getPublicationById( identifier );
        if( p != null ) {
            p.setStatus( status.toString() );
        }
        return p;
    }

    public void updatePublicationAdminGroup( String identifier, Operation operation, String group ) throws ImexCentralException {
        Publication p = getPublicationImexAccession( identifier, false );

        if (p != null){
            if (group != null && (group.equalsIgnoreCase(INTACT_GROUP) || group.equalsIgnoreCase(MATRIXDB_GROUP))){
                if (p.getAdminGroupList() == null){
                   p.setAdminGroupList(new Publication.AdminGroupList());
                }
                 p.getAdminGroupList().getGroup().add(group.toUpperCase());
            }
            // group not recognized, throw exception as in the real webservice
            else {
                ImexCentralFault imexFault = new ImexCentralFault();
                imexFault.setFaultCode(11);

                IcentralFault fault = new IcentralFault("Group not recognized", imexFault);
                throw new ImexCentralException(fault);
            }
        }
    }

    public void updatePublicationAdminUser( String identifier, Operation operation, String user ) throws ImexCentralException {
        Publication p = getPublicationImexAccession( identifier, false );

        if (p != null){
            if (user != null && (user.equalsIgnoreCase(intact_user) || user.equalsIgnoreCase(phantom_user))){
                if (p.getAdminUserList() == null){
                    p.setAdminUserList(new Publication.AdminUserList());
                }
                p.getAdminUserList().getUser().add(user.toLowerCase());
            }
            // user not recognized, throw exception as in the real webservice
            else {
                ImexCentralFault imexFault = new ImexCentralFault();
                imexFault.setFaultCode(10);

                IcentralFault fault = new IcentralFault("User not recognized", imexFault);
                throw new ImexCentralException(fault);
            }
        }
    }

    @Override
    public void updatePublicationIdentifier(String oldIdentifier, String newIdentifier) throws ImexCentralException {
        final Publication p = getPublicationById( oldIdentifier );
        if( p != null ) {
            for (Identifier id : p.getIdentifier()){
               if (oldIdentifier.equals(id.getAc())){
                   id.setAc(id.getAc());
                   id.setNs(id.getNs());
               }
            }

            if (p.getImexAccession() != null && p.getImexAccession().equalsIgnoreCase(oldIdentifier)){
                p.getIdentifier().add(buildIdentifier(newIdentifier));
            }
        }
    }

    public void createPublication( Publication publication ) throws ImexCentralException {
        Publication existingP = null;

        for ( Publication p : allPublications ) {
            for (Identifier i : p.getIdentifier()){
                if( !publication.getIdentifier().isEmpty() && publication.getIdentifier().iterator().next().getAc().equals( i.getAc() ) ) {
                    existingP = p;
                    break;
                }
            }
        }

        if (existingP == null){
            allPublications.add(publication);
        }
        else {
            allPublications.remove(existingP);
            allPublications.add(publication);
        }
    }

    public Publication createPublicationById( String identifier ) throws ImexCentralException {
        if (!Pattern.matches(PUBMED_REGEXP.toString(), identifier)){
            // no publication record created because not valid pubmed id, throw an exception (same behaviour as webservice)
            ImexCentralFault imexFault = new ImexCentralFault();
            imexFault.setFaultCode(7);

            IcentralFault fault = new IcentralFault("publication not created", imexFault);
            throw new ImexCentralException(fault);
        }

        Publication p = new Publication();
        final Identifier i = new Identifier();
        i.setAc( identifier );
        i.setNs("pmid");
        p.getIdentifier().add(i);
        p.setImexAccession( "N/A" );
        allPublications.add( p );
        return p;
    }

    public Publication getPublicationImexAccession( String identifier, boolean create ) throws ImexCentralException {
        final Publication p = getPublicationById( identifier );
        if( create ) {
            if( p.getImexAccession() != null && ! p.getImexAccession().equals("N/A")) {
                throw new IllegalStateException( "Publication already has an IMEx id: " + p.getImexAccession() );
            }

            // assigning new IMEx ID
            p.setImexAccession( "IM-" + imexIdSequence );
            imexIdSequence++;
        }

        return p;
    }

    protected static Identifier buildIdentifier( String identifier ) {
        final Identifier id = new ObjectFactory().createIdentifier();
        id.setAc( identifier );
        // IMEx identifier
        if( identifier.startsWith( "IM-" ) ) {
            // this will enable searching for publication by IMEx id ... not obvious but it works ...
            id.setNs( "imex" );
        }
        // valid pubmed identifier
        else if (Pattern.matches(pubmed_regexp.toString(), identifier)) {
            // fallback namespace
            id.setNs( "pubmed" );
        }
        // unassigned publication = internal identifier
        else if (identifier.startsWith("unassigned")){
            id.setNs( "jint" );
        }
        // doi number
        else {
            id.setNs("doi");
        }
        return id;
    }
}
