/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Encapsulation of a CSV data file.
 *
 * Supports both reading and creating CSV files.
 *
 * This class is NOT threadsafe.
 *
 * @author Steve Leach
 */
public class CSVFile implements Closeable, CSVRowSource {

    Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> headers = null;
    private Iterator<CSVRecord> iterator = null;
    private CSVRecord record = null;
    private CSVPrinter printer = null;
    private Closeable source = null;
    private int rowIndex = 0;
    private String GBP_UNICODE = "\u00A3";

    public interface CSVMapper<T> {
        T mapRow(CSVRowSource csv);
    }

    public CSVFile(Reader input) throws IOException {
        parse(input);
    }

    public CSVFile(InputStream stream) throws IOException {
        parse(new InputStreamReader(stream, "UTF-8"));
    }

    public CSVFile(URL url) throws IOException {
        parse(new InputStreamReader(url.openStream()));
        close();
    }

    public CSVFile(String content) throws IOException {
        parse(new StringReader(content));
        close();
    }

    public CSVFile(File file) throws IOException {
        parse(new FileReader(file));
        close();
    }

    /**
     * Creates a new CSV file for outputting, with the specified headers.
     */
    public CSVFile(Set<String> headers, Writer out) throws IOException {
        this.headers = headers;
        printer = CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])).print(out);
    }


    public CSVFile(final Set<String> headers,
                   final List<Map<String, Object>> data,
                   final Writer out) throws IOException {
        this.headers = headers;
        printer = CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])).print(out);
        data.forEach(item -> {
            try {writeValues(item);}
            catch (IOException e) {throw new RuntimeException(e);}});
    }

    /**
     * Loads data from a CSV file, mapping each row into an object using the supplied mapper.
     *
     * The CSV file must have a header row, and the mapper can retrieve values using the header values.
     */
    public static CSVFile fromResource(Object owner, String resourceName) throws IOException {
        InputStream stream = owner.getClass().getResourceAsStream(resourceName);
        if (stream == null) {
            throw new IOException("Unable to open resource: " + resourceName);
        }
        return new CSVFile(stream);
    }

    private void parse(Reader input) throws IOException {
        source = input;
        List<CSVRecord> records = CSVFormat.EXCEL.withIgnoreSurroundingSpaces().withIgnoreEmptyLines().withQuote('"').withHeader().withTrim().parse(input).getRecords();
        headers = records.get(0).toMap().keySet();
        iterator = records.iterator();
    }

    private void assertFileParsed() {
        if (iterator == null) {
            throw new IllegalStateException("CSV file has not been parsed");
        }
    }

    private void assertRowActive() {
        if (record == null) {
            throw new IllegalStateException("No active record");
        }
    }

    /**
     * Loads all the data from the CSV file, mapping each row to an object using the supplied mapper.
     */
    public <T> List<T> loadData(CSVMapper<T> mapper) {
        List<T> results = new LinkedList<T>();
        while (nextRow()) {
            results.add(mapper.mapRow(this));
        }
        return results;
    }

    /**
     * Loads the next row from the CSV file, if one exists.
     *
     * No attempt should be made to get values from the CSV if this method returns false.
     *
     * @return true if there is another row, false otherwise
     */
    public boolean nextRow() {
        assertFileParsed();
        if (iterator.hasNext()) {
            record = iterator.next();
            rowIndex++;
            return true;
        } else {
            record = null;
            return false;
        }
    }

    /**
     * Returns the value from the current row corresponding to the specified header, as string.
     *
     * @throws IllegalArgumentException if the specified key is not found in the header row
     */
    public String getString(String key) {
        assertRowActive();
        return record.get(key);
    }

    /**
     * Returns the value from the current row corresponding to the specified header, as an integer.
     *
     * @throws NumberFormatException
     */
    public int getInteger(String key) {

        try {
            return Integer.parseInt(getString(key));
        } catch (NumberFormatException e) {
            log.error("Number format exception parsing {} for {}", getString(key), key);
            throw e;
        }
    }

/**
     * Returns the value from the current row corresponding to the specified header, as an integer.
     *
     * @throws NumberFormatException
     */
    public Integer getIntegerOrNull(String key) {
        String value = getString(key);
        if (!StringUtils.isEmpty(value)) {
            return getInteger( key);
        }
        return null;
    }

 /**
     * Returns the value from the current row corresponding to the specified header, as an integer rounded from a decimal.
     *
     * @throws NumberFormatException
     */
    public int getRoundedDecimal(String key) {
        String value = getString(key);
        if (value == null) {
            return 0;
        }
        value = value.replaceAll(getStripNonNumericRegex(), "");

        return (int) Math.round(Double.parseDouble(value));
    }


    public BigDecimal getCurrencyValue(String key) {
        String value = getString(key);
        if ((value == null) || (value.trim().length() == 0)) {
            return null;
        }

        value = value.replaceAll(getStripNonNumericRegex(), "");

        if (value.trim().length() == 0 || value.trim().equals("-")) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.error("Number format exception parsing {} for {}", value, key);
            throw e;
        }
    }

    private String getStripNonNumericRegex() {
        // anything not digit, decimal point or minus sign
        return "[^\\d\\.\\-]";
    }

    public LocalDate getDate(String key, String format) {
        String value = getString(key);
        if ((value == null) || (value.trim().length() == 0)) {
            return null;
        }

        return LocalDate.parse(value,  DateTimeFormatter.ofPattern(format));
    }




    public List<Object> makeRecordWithCurrentHeaders(Map<String, Object> projectAsMap) {
        if (this.headers == null) {
            throw new IllegalStateException("Headers not specified for CSV output");
        }
        return headers.stream().map(projectAsMap::get).collect(Collectors.toList());
    }

    public void writeValues(Map<String, Object> valueMap) throws IOException {
        List<Object> values = makeRecordWithCurrentHeaders(valueMap);
        printer.printRecord(values);
    }

    public void writeEmptyLine() throws IOException {
        printer.println();
    }

    public String getCurrentRowSource() throws IOException {
        StringWriter out = new StringWriter();
        CSVPrinter outPrinter = new CSVPrinter(out, CSVFormat.EXCEL.withIgnoreSurroundingSpaces().withQuote('\"'));
        outPrinter.print(record.toMap());
        outPrinter.close();
        return out.toString();
    }

    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public void close() throws IOException {
        if (source != null) {
            source.close();
        }
    }

    public void closeAlways() {
        try {
            this.close();
        } catch (IOException e) {
            log.warn("Ignoring IOException while closing CSVFile");
        }
    }

    public Set<String> getHeaders() {
        return Collections.unmodifiableSet(headers);
    }
}
