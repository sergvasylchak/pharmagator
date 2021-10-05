package com.eleks.academy.pharmagator;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Getter
@SpringBootTest
class PharmagatorApplicationTests {

	record Circle(int i, int j) {
	}

	@Test
	void contextLoads() {
		final var circle = new Circle(0, 0);

		assertEquals(0, circle.i());
	}

	@Test
	void contextLoads2() {
		final var circle = new Circle(0, 0);

		assertEquals(1, circle.i());
	}

}
