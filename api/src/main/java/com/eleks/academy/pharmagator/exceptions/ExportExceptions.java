package com.eleks.academy.pharmagator.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExportExceptions extends RuntimeException {

    private final Error error;

    @Getter
    @AllArgsConstructor
    public enum Error {
        GET_PDF_WRITER_IS_BAD("Cannot get instance of PDFWriter"),
        INVALID_FONT("Cannot create font"),
        WRITE_TO_CSV_IS_BAD("Writing data to bytes was unsuccessful");

        private String message;
    }

}
