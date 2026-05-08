package ca.concordia.encs.citydata.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Energy Star Portfolio Manager API. Credentials and base URL are loaded from application.properties at startup
 * Author: Minette Zongo
 * Date: 2026-02-24
 */
@Configuration
public class PortfolioManagerConfig {

    private static String baseUrl;
    private static String username;
    private static String password;

    @Value("${portfoliomanager.base-url}")
    public void setBaseUrl(String value)  { PortfolioManagerConfig.baseUrl = value; }

    @Value("${portfoliomanager.username}")
    public void setUsername(String value) { PortfolioManagerConfig.username = value; }

    @Value("${portfoliomanager.password}")
    public void setPassword(String value) { PortfolioManagerConfig.password = value; }

    public static String getBaseUrl()  { return baseUrl; }
    public static String getUsername() { return username; }
    public static String getPassword() { return password; }
}
