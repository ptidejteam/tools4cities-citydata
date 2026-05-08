package ca.concordia.encs.citydata.core.controller;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.util.StringUtils;

/**
 * This class is to print all available operations and their characteristics
 *
 * @author Sikandar Ejaz and Gabriel C. Ullmann
 * @since 2025-06-02
 */

@RestController
@RequestMapping("/operations")
public class ListOperationsController {

	private static final String OPERATIONS_BASE_PACKAGE = "ca.concordia.encs.citydata.operations";

	@GetMapping("/list")
	public String listOperations() {
		final JsonArray operationDetailsList = new JsonArray();

		try {
			// This scanner implementation works both on the filesystem and inside a JAR
			ClassPathScanningCandidateComponentProvider scanner =
					new ClassPathScanningCandidateComponentProvider(false);

			scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

			for (BeanDefinition bd : scanner.findCandidateComponents(OPERATIONS_BASE_PACKAGE)) {
				final String className = bd.getBeanClassName();
				final Class<?> clazz = Class.forName(className);

				final JsonObject operationDetails = new JsonObject();
				operationDetails.addProperty("name", clazz.getName());

				final Method[] methods = clazz.getMethods();
				final List<String> paramList = StringUtils.getParamDescriptions(methods);
				operationDetails.addProperty("params", String.join(", ", paramList));

				operationDetailsList.add(operationDetails);
			}

		} catch (ClassNotFoundException e) {
			final JsonObject errorObject = new JsonObject();
			errorObject.addProperty("error", e.getMessage());
			operationDetailsList.add(errorObject);
		}

		return operationDetailsList.toString();
	}
}