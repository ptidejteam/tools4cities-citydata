package ca.concordia.encs.citydata.core.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT Authentication Implementation
 * @author Sikandar Ejaz 
 * @since 18-07-2025
 */

@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) {

}
