package ca.concordia.encs.citydata.operations;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

/**
 * This operation converts XML meter consumption data from Portfolio Manager
 * into an array of JsonObjects, with optional filtering by a given key and value.
 *
 * @author: Minette Zongo
 * @since 2026-02-26
 */
public class XmlToJsonFilterOperation extends AbstractOperation<JsonObject> implements IOperation<JsonObject> {

    String filterKey;
    String filterValue;
    String from;
    String to;
    Boolean isExactlyEqual = false;

    public void setFilterKey(String filterKey) {
        this.filterKey = filterKey;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public void setFrom(String from) {
    	System.out.println("setFrom received: '" + from + "'");
        this.from = from;
    }

    public void setTo(String to) {
        System.out.println("setTo received: '" + to + "'");
        this.to = to;
    }

    public void setIsExactlyEqual(Boolean isExactlyEqual) {
        this.isExactlyEqual = isExactlyEqual;
    }

    @Override
    public ArrayList<JsonObject> apply(ArrayList<JsonObject> inputs) {
        final ArrayList<JsonObject> resultList = new ArrayList<>();

        for (JsonObject wrapper : inputs) {
        	System.out.println("Wrapper keys: " + wrapper.keySet());
            System.out.println("Wrapper content: " + wrapper.toString());
            if (!wrapper.has("xml")) continue;

            final String xmlString = wrapper.get("xml").getAsString();

            try {
                final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                final Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
                final NodeList entries = doc.getElementsByTagName("meterConsumption");

                for (int i = 0; i < entries.getLength(); i++) {
                    final Element entry = (Element) entries.item(i);
                    final JsonObject obj = new JsonObject();
                    obj.addProperty("meterId",   wrapper.get("meterId").getAsString());
                    obj.addProperty("startDate", getTag(entry, "startDate"));
                    obj.addProperty("endDate",   getTag(entry, "endDate"));
                    obj.addProperty("usage",     getTag(entry, "usage"));

                    // filtering is optional — if filterKey not set, keep all records
                    if (filterKey == null || filterKey.isEmpty()) {
                        resultList.add(obj);
                    } else if (obj.has(filterKey)) {
                        final String objectValue = obj.get(filterKey).getAsString();
                        if (from != null && to != null) {
                            if (objectValue.compareTo(from) >= 0 && objectValue.compareTo(to) <= 0) {
                                resultList.add(obj);
                                System.out.println("objectValue: " + objectValue);
                                System.out.println("from: " + from + " | to: " + to);
                                System.out.println("compareTo from: " + objectValue.compareTo(from));
                                System.out.println("compareTo to: " + objectValue.compareTo(to));
                            }
                        } else if (filterValue != null && isExactlyEqual && objectValue.equals(filterValue)) {
                        	resultList.add(obj);
                        } else if (filterValue != null && !isExactlyEqual && objectValue.contains(filterValue)) {
                            resultList.add(obj);
                    }          
                }
            } catch (Exception e) {
                System.err.println("XmlToJsonFilterOperation parse error: " + e.getMessage());
            }
        }
        System.out.println("Result size: " + resultList.size());
        return resultList;
    }

    private String getTag(Element el, String tag) {
        final NodeList list = el.getElementsByTagName(tag);
        return (list.getLength() > 0) ? list.item(0).getTextContent() : "";
    }
}
