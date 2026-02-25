package ca.concordia.encs.citydata.core.controllers;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.utils.Constants;
import ca.concordia.encs.citydata.core.utils.StringUtils;

/**
 * This route prints available producers and their characteristics in CITYdata
 * 
 * @author Sikandar Ejaz
 * @since 2025-06-02
 */

@RestController
@RequestMapping("/producers")
public class ListProducerController {

	@GetMapping("/list")
	public String listProducers() {
		final JsonArray producerDetailsList = new JsonArray();
		final String projectRootPath = Paths.get("").toAbsolutePath() + "/";

		final Map<String, String> packagesToScan = new LinkedHashMap<>();
		packagesToScan.put(projectRootPath + Constants.PRODUCER_ROOT_PACKAGE, "ca.concordia.encs.citydata.producers.");
		packagesToScan.put(projectRootPath + Constants.PRODUCER_ROOT_PACKAGE + "base/",
				"ca.concordia.encs.citydata.producers.base.");

		try {
			final String fileExtension = ".java";

			for (Map.Entry<String, String> entry : packagesToScan.entrySet()) {
				final String packagePath = entry.getKey();
				final String classPrefix = entry.getValue();

				final File[] files = new File(packagePath).listFiles((dir, name) -> name.endsWith(fileExtension));

				if (files != null) {
					for (File file : files) {
						final String className = file.getName().replace(fileExtension, "");
						final Class<?> clazz = Class.forName(classPrefix + className);

						final JsonObject operationDetails = new JsonObject();
						operationDetails.addProperty("name", clazz.getName());

						final Method[] methods = clazz.getMethods();
						final List<String> paramList = StringUtils.getParamDescriptions(methods);
						operationDetails.addProperty("params", String.join(", ", paramList));
						producerDetailsList.add(operationDetails);
					}
				}
			}

		} catch (ClassNotFoundException e) {
			final JsonObject errorObject = new JsonObject();
			errorObject.addProperty("error", e.getMessage());
			producerDetailsList.add(errorObject);
		}

		return producerDetailsList.toString();
	}
}