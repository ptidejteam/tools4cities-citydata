package ca.concordia.encs.citydata.test.producers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.concordia.encs.citydata.producers.FCUProducer;

/**
 * FCUProducer Tests
 *
 * @author @author Peter Yefi, Vinicius Mioto, Tahereh Bijani,  Mohamed Jendoubi
 * @since 2026-05-27
 * @author: Minette Z. Fixed the test by changing the imports, and using the right assert (assertEquals)
 * @date: 2026-05-29
 */

public class FCUProducerTest {

	private FCUProducer fcuProducer = null;
	private final String stringFilePath = "./src/test/resources/sample_fcu.csv";

	@BeforeEach
	void setUp() {
		fcuProducer = new FCUProducer(stringFilePath);
	}

	@Test
	void testThatFilePathMatch() {
		//Arrange and act
		fcuProducer.fetch();
		assertEquals(fcuProducer.getFilePath(), stringFilePath);
	}

	@Test
	void testThatResultSetMatchesFileContent() {
		fcuProducer.fetch();

		String[] rowOne = fcuProducer.getResult().getFirst().split(",");
		String[] lastRow = fcuProducer.getResult().getLast().split(",");

		assertEquals(fcuProducer.getResult().size(), 19);
		assertEquals(rowOne.length, 13);
		assertEquals(rowOne[0], "2022-03-02 02:15:00-05:00");
		assertEquals(lastRow[12], "-0.6838173");
	}
}
