package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.dataproviders.dto.MedicineDto;
import com.eleks.academy.pharmagator.entities.Pharmacy;
import com.eleks.academy.pharmagator.exceptions.ExportExceptions;
import com.eleks.academy.pharmagator.exceptions.InvalidIdentifierException;
import com.eleks.academy.pharmagator.exceptions.UploadExceptions;
import com.eleks.academy.pharmagator.projections.MedicinePrice;
import com.eleks.academy.pharmagator.repositories.PharmacyRepository;
import com.eleks.academy.pharmagator.repositories.PriceRepository;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CsvServiceImpl implements CsvService {

    private final CsvParser parser;
    private final BeanListProcessor<MedicineDto> rowProcessor;
    private final ImportService importService;
    private static final String TYPE = "text/csv";

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PharmacyRepository pharmacyRepository;
    private final PriceRepository priceRepository;
    private static final String DELIMITER = ",";

    @Override
    public String parseAndSave(MultipartFile file) {
        if (Objects.equals(file.getContentType(), TYPE)) {
            try {
                InputStream inputStream = file.getInputStream();
                parser.parse(inputStream);
                rowProcessor.getBeans().forEach(importService::storeToDatabase);
                return "Save of file " + file.getOriginalFilename() + " was successful";
            } catch (IOException e) {
                throw new UploadExceptions(UploadExceptions.Error.SAVE_WAS_NOT_SUCCESSFUL);
            }
        }
        throw new UploadExceptions(UploadExceptions.Error.INVALID_FILE_FORMAT);
    }

    @Override
    public byte[] export() {
        addHeader();
        addRows();
        return byteArrayOutputStream.toByteArray();
    }

    private void addHeader() {
        Arrays.stream(createHeader())
                .map(String::getBytes)
                .forEach(this::write);
    }

    private String[] createHeader() {
        return new String[]{"title" + DELIMITER, "price" + DELIMITER, "pharmacyName"};
    }

    private void addRows() {
        priceRepository.findAllMedicinesPrices().stream()
                .map(this::createRow)
                .forEach(row -> row.forEach(this::write));
    }

    private List<byte[]> createRow(MedicinePrice medicinePrice) {
        List<byte[]> bytesList = new ArrayList<>();

        byte[] price = medicinePrice.getPrice().toString().getBytes(StandardCharsets.UTF_8);

        Pharmacy pharmacy = pharmacyRepository.findById(medicinePrice.getPharmacyId())
                .orElseThrow(InvalidIdentifierException::new);

        bytesList.add("\n".getBytes());
        bytesList.add(medicinePrice.getTitle().getBytes(StandardCharsets.UTF_8));
        bytesList.add(DELIMITER.getBytes());
        bytesList.add(price);
        bytesList.add(DELIMITER.getBytes());
        bytesList.add(pharmacy.getName().getBytes(StandardCharsets.UTF_8));

        return bytesList;
    }

    private void write(byte[] bytes) {
        try {
            byteArrayOutputStream.write(bytes);
        } catch (IOException e) {
            throw new ExportExceptions(ExportExceptions.Error.WRITE_TO_CSV_IS_BAD);
        }
    }
    
}

