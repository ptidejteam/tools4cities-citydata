package ca.concordia.encs.citydata.core.controllers;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.utils.StringUtils;

/**
 * This class is to print all available producers and their characteristics
 *
 * @author Sikandar Ejaz and Gabriel C. Ullmann
 * @since 2025-06-02
 */
@RestController
@RequestMapping("/producers")
public class ListProducerController {

	private static final String PRODUCER_BASE_PACKAGE = "ca.concordia.encs.citydata.producers";

	@GetMapping("/list")
	public String listProducers() {
		final JsonArray producerDetailsList = new JsonArray();

		try {
			// This scanner implementation works both on the filesystem and inside a JAR
			ClassPathScanningCandidateComponentProvider scanner =
					new ClassPathScanningCandidateComponentProvider(false);

			scanner.addIncludeFilter(new RegexPatternTypeFilter(
					java.util.regex.Pattern.compile(".*")));

			for (org.springframework.beans.factory.config.BeanDefinition bd
					: scanner.findCandidateComponents(PRODUCER_BASE_PACKAGE)) {

				final String className = bd.getBeanClassName();
				final Class<?> clazz = Class.forName(className);

				final JsonObject operationDetails = new JsonObject();
				operationDetails.addProperty("name", clazz.getName());

				final Method[] methods = clazz.getMethods();
				final List<String> paramList = StringUtils.getParamDescriptions(methods);
				operationDetails.addProperty("params", String.join(", ", paramList));

				producerDetailsList.add(operationDetails);
			}

		} catch (ClassNotFoundException e) {
			final JsonObject errorObject = new JsonObject();
			errorObject.addProperty("error", e.getMessage());
			producerDetailsList.add(errorObject);
		}

		return producerDetailsList.toString();
	}
}