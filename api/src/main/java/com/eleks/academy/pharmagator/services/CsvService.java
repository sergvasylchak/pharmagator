package com.eleks.academy.pharmagator.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CsvService {

    String parseAndSave(MultipartFile file);

    byte[] export() throws IOException;
}
