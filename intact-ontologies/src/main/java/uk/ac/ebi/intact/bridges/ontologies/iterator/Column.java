package uk.ac.ebi.intact.bridges.ontologies.iterator;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

enum Column { // Order of declaration will be order in the result of the query
    ID("id", true),
    MNEMONIC("mnemonic", true),
    SCIENTIFIC_NAME("scientific_name", true),
    COMMON_NAME("common_name", true),
    SYNONYMS("synonyms", true),
    OTHER_NAMES("other_names", true),
    REVIEWED("reviewed", true),
    RANK("rank", true),
    LINEAGE("lineage", true),
    PARENT("parent", true),

    STRAINS("strains", false),
    VIRAL_HOSTS("hosts", false),
    LINKS("links", false),
    STATISTICS("statistics", false);

    public final String name;
    public final boolean toBeQueried;
    public int index = -1;
    private static List<Column> columnList;
    private static String columnString;


    Column(String name, boolean toBeQueried) {
        this.name = name;
        this.toBeQueried = toBeQueried;
    }

    public static List<Column> columnList() {
        if (columnList == null) {
            columnList = Arrays.stream(Column.values()).filter(column -> column.toBeQueried).collect(Collectors.toList());
            for (int i = 0; i < columnList.size(); i++) {
                columnList.get(i).index = i;
            }
        }
        return columnList;
    }

    public static String columnString() {
        if (columnString == null) columnString = "&format=tsv&fields=" + URLEncoder.encode(Column.columnList().stream().map(column -> column.name).collect(Collectors.joining(",")), StandardCharsets.UTF_8);
        return columnString;
    }
}
