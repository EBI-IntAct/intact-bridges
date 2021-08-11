/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.uniprot.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Cross reference of a UniProt protein.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-Sep-2006</pre>
 */
public class UniprotXref implements Serializable {

    /////////////////////////
    // Instance attributes

    private static final long serialVersionUID = -3414917566136587359L;

    /**
     * Accession number of the cross reference.
     */
    private String accession;

    /**
     * Database of the cross reference.
     */
    private String database;

    /**
     * Additional description of the cross reference.
     */
    private String description;

    /**
     * Context of the type of identifier (it helps in case of ambiguity e.g. transcript and protein identifiers are the same.
     */
    private String qualifier;

    /**
     * Isoform specific information (if available)
     */
    private String isoformId;

    //////////////////////
    // Constructor
    public UniprotXref(String ac, String db, String description, String qualifier, String isoformId) {

        if ( ac == null || ac.trim().isEmpty()) {
            throw new IllegalArgumentException( "A cross reference's AC cannot be null or empty." );
        }
        this.accession = ac;

        if ( db == null || db.trim().isEmpty()) {
            throw new IllegalArgumentException( "A cross reference's database cannot be null or empty." );
        }
        this.database = db;
        this.description = description;
        this.qualifier = qualifier;
        this.isoformId = isoformId;
    }

    public UniprotXref(String ac, String db, String description, String qualifier) {
        this(ac, db, description, qualifier, null);
    }

    public UniprotXref(String ac, String db, String description) {
        this(ac, db, description, null);
    }

    public UniprotXref( String ac, String db ) {
        this(ac, db, null);
    }

    ///////////////////////////
    // Getters and Setters

    public String getAccession() {
        return accession;
    }

    public void setAccession( String accession ) {
        if ( accession == null ) {
            throw new IllegalArgumentException( "An Xref must have a non null accession." );
        }
        if ( accession.trim().length() == 0 ) {
            throw new IllegalArgumentException( "An Xref must have a non empty accession." );
        }
        this.accession = accession;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase( String database ) {
        if ( database == null ) {
            throw new IllegalArgumentException( "An Xref must have a non null database." );
        }
        if ( database.trim().length() == 0 ) {
            throw new IllegalArgumentException( "An Xref must have a non empty database." );
        }
        this.database = database;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getIsoformId() {
        return isoformId;
    }

    public void setIsoformId(String isoformId) {
        this.isoformId = isoformId;
    }

    ////////////////////////////
    // Object's override

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniprotXref that = (UniprotXref) o;
        return accession.equals(that.accession) && database.equals(that.database) && Objects.equals(description, that.description) && Objects.equals(qualifier, that.qualifier) && Objects.equals(isoformId, that.isoformId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accession, database, description, qualifier, isoformId);
    }

    @Override
    public String toString() {
        return "UniprotXref{" +
                "accession='" + accession + '\'' +
                ", database='" + database + '\'' +
                ", description='" + description + '\'' +
                ", qualifier='" + qualifier + '\'' +
                ", isoformId='" + isoformId + '\'' +
                '}';
    }
}