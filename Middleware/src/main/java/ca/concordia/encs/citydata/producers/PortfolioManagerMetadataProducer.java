package ca.concordia.encs.citydata.producers;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.configs.PortfolioManagerConfig;
import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
 * Producer for fetching Portfolio Manager metadata (account, property, or meter) and forwarding the XML response to the
 * PortfolioManagerProducer for further processing.
 * @author Minette Zongo
 * @since 2026-02-24
 */

public class PortfolioManagerMetadataProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {
    private String dataType;
    private String accountId;
    private String propertyId;
    private String meterId;
    private IOperation<JsonObject> operation;
    private IRunner runnerObserver;

    public void setAccountId(String accountId)   { this.accountId = accountId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    public void setMeterId(String meterId)       { this.meterId = meterId; }

    public void setDataType(String dataType) {
        this.dataType = dataType.toUpperCase();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setOperation(IOperation operation) {
        this.operation = operation;
    }

    @Override
    public void addObserver(IRunner aRunner) {
        this.runnerObserver = aRunner;
    }


    @Override
    public void fetch() {
        if (this.dataType == null) {
            throw new RuntimeException("'dataType' is required. "
                    + "Valid values: ACCOUNT, PROPERTY_LIST, PROPERTY, METER_LIST, METER");
        }

        final String endpoint = resolveEndpoint();
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setMethod("GET");
        requestOptions.addToHeaders("Authorization", buildBasicAuth());
        requestOptions.addToHeaders("Accept", "application/xml");

        this.setFilePath(endpoint);
        this.setFileOptions(requestOptions);

        final String xmlResponse = this.fetchFromPath().toString();

        final JsonObject wrapper = new JsonObject();
        wrapper.addProperty("xml", xmlResponse);
        wrapper.addProperty("endpoint", endpoint);

        final ArrayList<JsonObject> result = new ArrayList<>();
        result.add(wrapper);
        this.setResult(result);
        super.addObserver(this.runnerObserver);
        super.setOperation(this.operation);
        this.applyOperation();
    }

    private String resolveEndpoint() {
        String base = PortfolioManagerConfig.getBaseUrl();
        return switch (this.dataType) {
            case "ACCOUNT" ->
                base + "/account";
            case "PROPERTY_LIST" ->
                base + "/account/" + requireId(accountId, "accountId") + "/property/list";
            case "PROPERTY" ->
                base + "/property/" + requireId(propertyId, "propertyId");
            case "METER_LIST" ->
                base + "/property/" + requireId(propertyId, "propertyId") + "/meter/list";
            case "METER" ->
                base + "/meter/" + requireId(meterId, "meterId");
            default -> throw new RuntimeException("Unknown dataType: " + this.dataType
                    + ". Valid: ACCOUNT, PROPERTY_LIST, PROPERTY, METER_LIST, METER");
        };
    }

    private String buildBasicAuth() {
        return "Basic " + java.util.Base64.getEncoder()
                .encodeToString(
                    (PortfolioManagerConfig.getUsername() + ":"
                    + PortfolioManagerConfig.getPassword())
                    .getBytes(StandardCharsets.UTF_8));
    }

    private String requireId(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new RuntimeException("'" + name + "' is required for dataType: "
                    + this.dataType);
        }
        return id;
    }

}
