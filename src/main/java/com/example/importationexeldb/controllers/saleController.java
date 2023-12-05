package com.example.importationexeldb.controllers;

import com.example.importationexeldb.services.Sales_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@CrossOrigin
@RestController
@RequestMapping("/excel")
public class saleController {
    @Autowired
    private final Sales_service sales_service;

    @Autowired
    public saleController(Sales_service sales_service) {
        this.sales_service = sales_service;
    }

    @PostMapping("/upload")
    public void uploadExcelFile(@RequestParam("file") MultipartFile file) {
        try (InputStream fileStream = file.getInputStream()) {
            sales_service.processExcelFile(fileStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
