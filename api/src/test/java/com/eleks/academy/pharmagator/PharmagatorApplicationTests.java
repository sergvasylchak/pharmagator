package com.eleks.academy.pharmagator;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Getter
@SpringBootTest
class PharmagatorApplicationTests {

	@Test
	void contextLoads() {
		assertEquals(1, 9);
	}
}
