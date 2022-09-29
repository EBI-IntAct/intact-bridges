/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.bridges.ontologies.iterator;

import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Example URL: https://www.uniprot.org/taxonomy/?query=*&limit=10&format=list
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UniprotTaxonomyOntologyIterator extends LineOntologyIterator {

    private static final String BASE_URL = "https://rest.uniprot.org/taxonomy";
    private static final String STREAM_URL = BASE_URL + "/stream";
    private static final String PAGINATED_URL = BASE_URL + "/search";

    static {
        Column.columnString();
    }

    private URL nextUrl;

    public UniprotTaxonomyOntologyIterator(URL url) throws IOException {
        super(url);
        String link = this.header.get("Link").get(0);
        this.nextUrl = new URL(link.substring(link.indexOf("<") + 1, link.indexOf(">")));
    }

    public UniprotTaxonomyOntologyIterator(InputStream is) {
        super(is);
    }

    public UniprotTaxonomyOntologyIterator(Reader reader) {
        super(reader);
    }

    public UniprotTaxonomyOntologyIterator() throws IOException {
        this("*", -1);
    }

    public UniprotTaxonomyOntologyIterator(int limit) throws IOException {
        this("*", limit);
    }

    public UniprotTaxonomyOntologyIterator(String query, int limit) throws IOException {
        this(query, limit, false);
    }

    public UniprotTaxonomyOntologyIterator(String query, int limit, boolean onlyReviewed) throws IOException {
        this(new URL((limit < 0 ? STREAM_URL : PAGINATED_URL) + "?query=" +
                URLEncoder.encode(query + (onlyReviewed ? " AND reviewed:yes" : ""), StandardCharsets.UTF_8.name()) +
                Column.columnString() +
                (limit < 0 ? "" : String.format("&size=%d", limit))));
    }

    public UniprotTaxonomyOntologyIterator nextPage() throws IOException {
        return new UniprotTaxonomyOntologyIterator(nextUrl);
    }

    @Override
    public boolean skipLine(String line) {
        String[] cols = line.split("\t");

        if (line.startsWith("Taxon") || cols.length < 4) {
            return true;
        }

        return super.skipLine(line);
    }

    /**
     * @param line The line to process
     * @return the ontology document
     */
    protected OntologyDocument processLine(String line) {
        String[] cols = line.split("\t");

        String childId = safeGet(cols, Column.ID.index);

        String scientificName = safeGet(cols, Column.SCIENTIFIC_NAME.index);
        String commonName = safeGet(cols, Column.COMMON_NAME.index);
        String synonym = safeGet(cols, Column.SYNONYMS.index);
        String[] otherNames = split(safeGet(cols, Column.OTHER_NAMES.index));

        String childName = scientificName;

        /*if (commonName != null && commonName.length() > 0) {
            childName = commonName;
        } else {
            childName = scientificName;
        }*/

        String parentId = safeGet(cols, Column.PARENT.index);
        String parentName = "";
        String lineage = safeGet(cols, Column.LINEAGE.index);

        // the parent name is the last element in the lineage
        if (lineage.lastIndexOf(";") > -1) {
            parentName = lineage.substring(lineage.lastIndexOf(";") + 1).trim();
        }

        OntologyDocument doc = new OntologyDocument("uniprot taxonomy", parentId, parentName,
                childId, childName, "OBO_REL:is_a", false);

        //doc.addChildSynonym(scientificName);
        doc.addChildSynonym(commonName);
        doc.addChildSynonym(synonym);
        doc.addAllChildSynonyms(otherNames);

        return doc;
    }

    private String[] split(String s) {
        if (s == null || s.isEmpty()) return new String[0];

        return s.split("; ");
    }

    private String safeGet(String[] cols, int index) {
        if (cols.length > index) {
            return cols[index];
        } else {
            return "";
        }
    }

}
