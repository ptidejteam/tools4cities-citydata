package ca.concordia.encs.citydata.core.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a welcome page for CITYdata, intended for access via browser.
 *
 * @author Gabriel C. Ullmann , Rushin D. Makwana
 * @since 2025-08-27
 */

@RestController
public class HomepageController {

	@GetMapping("/home")
	@ResponseBody
	public String home() throws IOException {
		var resource = new ClassPathResource("static/home.html");
		return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
	}
}