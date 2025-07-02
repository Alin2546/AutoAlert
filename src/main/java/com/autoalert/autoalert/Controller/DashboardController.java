package com.autoalert.autoalert.Controller;

import com.autoalert.autoalert.Model.Car;
import com.autoalert.autoalert.Model.CarDocument;
import com.autoalert.autoalert.Model.Dto.CarDocumentDto;
import com.autoalert.autoalert.Model.Dto.CarDto;
import com.autoalert.autoalert.Service.CarDocumentService;
import com.autoalert.autoalert.Service.CarService;
import com.autoalert.autoalert.Service.UserService;
import com.autoalert.autoalert.Service.VehicleApiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.util.List;


import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CarService carService;
    private final CarDocumentService carDocumentService;
    private final UserService userService;
    private final VehicleApiService vehicleApiService;


    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();

        List<Car> userCars = carService.findByUserEmail(email);
        Map<Long, String> carStatus = new HashMap<>();
        for (Car car : userCars) {
            String status = carService.calculateCarStatus(car);
            carStatus.put(car.getId(), status);
        }

        List<String> brands = vehicleApiService.getAllMakes();
        Map<String, List<String>> modelsByBrand = vehicleApiService.getModelsGroupedByBrand();
        ObjectMapper objectMapper = new ObjectMapper();
        String modelsByBrandJson = null;
        try {
            modelsByBrandJson = objectMapper.writeValueAsString(modelsByBrand);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("brands", brands);
        model.addAttribute("modelsByBrand", modelsByBrand);
        model.addAttribute("modelsByBrandJson", modelsByBrandJson);
        model.addAttribute("cars", userCars);
        model.addAttribute("carStatus", carStatus);
        model.addAttribute("reminders", carDocumentService.generateRemindersForUser(email));
        model.addAttribute("carDto", new CarDto());

        return "dashboard";
    }




    @PostMapping("/dashboard/cars/add")
    public String addCar(@Valid @ModelAttribute("carDto") CarDto carDto,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) throws JsonProcessingException {

        String email = userDetails.getUsername();

        List<Car> userCars = carService.findByUserEmail(email);
        boolean isPremium = userService.isUserPremium(email);

        List<String> brands = vehicleApiService.getAllMakes();
        Map<String, List<String>> modelsByBrand = vehicleApiService.getModelsGroupedByBrand();

        model.addAttribute("brands", brands);
        model.addAttribute("modelsByBrand", modelsByBrand);


        String modelsByBrandJson = new ObjectMapper().writeValueAsString(modelsByBrand);
        model.addAttribute("modelsByBrandJson", modelsByBrandJson);

        if (!vehicleApiService.isValidCar(carDto.getBrand(), carDto.getModel())) {
            result.rejectValue("brand", "error.carDto", "Marca sau modelul introdus nu este valid.");
        }

        if (!isPremium && userCars.size() >= 1) {
            model.addAttribute("showPremiumModal", true);
            model.addAttribute("error", "Trebuie să fii utilizator PREMIUM pentru a adăuga mai multe mașini.");
        }

        if (result.hasErrors() || (!isPremium && userCars.size() >= 1)) {
            model.addAttribute("cars", userCars);

            Map<Long, String> carStatus = new HashMap<>();
            for (Car car : userCars) {
                String status = carService.calculateCarStatus(car);
                carStatus.put(car.getId(), status);
            }
            model.addAttribute("carStatus", carStatus);

            List<String> reminders = carDocumentService.generateRemindersForUser(email);
            model.addAttribute("reminders", reminders);

            model.addAttribute("carDto", carDto);
            return "dashboard";
        }

        carService.addCarToUser(carDto, email);
        return "redirect:/dashboard";
    }




    @GetMapping("/dashboard/cars/{carId}/documents")
    public String showCarDocuments(@PathVariable int carId, Model model) {
        Car car = carService.findById(carId);
        List<CarDocument> documents = carDocumentService.findByCarId(carId);
        model.addAttribute("car", car);
        model.addAttribute("documents", documents);
        model.addAttribute("carDocumentDto", new CarDocumentDto());
        return "carDocuments";
    }


    @PostMapping("/dashboard/cars/{carId}/documents/add")
    public String addCarDocument(@PathVariable int carId,
                                 @Valid @ModelAttribute("carDocumentDto") CarDocumentDto carDocumentDto,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            Car car = carService.findById(carId);
            List<CarDocument> documents = carDocumentService.findByCarId(carId);
            model.addAttribute("car", car);
            model.addAttribute("documents", documents);
            return "carDocuments";
        }

        try {
            carDocumentService.addDocumentToCar(carId, carDocumentDto);
        } catch (DataIntegrityViolationException e) {
            Car car = carService.findById(carId);
            List<CarDocument> documents = carDocumentService.findByCarId(carId);
            model.addAttribute("car", car);
            model.addAttribute("documents", documents);
            model.addAttribute("errorMessage", "Documentul de acest tip există deja pentru această mașină.");
            return "carDocuments";
        } catch (RuntimeException e) {
            Car car = carService.findById(carId);
            List<CarDocument> documents = carDocumentService.findByCarId(carId);
            model.addAttribute("car", car);
            model.addAttribute("documents", documents);
            model.addAttribute("errorMessage", e.getMessage());
            return "carDocuments";
        }

        return "redirect:/dashboard/cars/" + carId + "/documents";
    }

    @GetMapping("/dashboard/cars/export")
    public void exportCarsToExcel(@AuthenticationPrincipal UserDetails userDetails,
                                  HttpServletResponse response) throws Exception {
        String email = userDetails.getUsername();
        List<Car> cars = carService.findByUserEmail(email);


        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=masini.xlsx";
        response.setHeader(headerKey, headerValue);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Masini");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Marca");
        headerRow.createCell(1).setCellValue("Model");
        headerRow.createCell(2).setCellValue("Număr Înmatriculare");

        int rowCount = 1;
        for (Car car : cars) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(car.getBrand());
            row.createCell(1).setCellValue(car.getModel());
            row.createCell(2).setCellValue(car.getLicensePlate());
        }

        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/dashboard/cars/export/pdf")
    public void exportCarsToPdf(@AuthenticationPrincipal UserDetails userDetails,
                                HttpServletResponse response) throws IOException, DocumentException {
        String email = userDetails.getUsername();
        List<Car> cars = carService.findByUserEmail(email);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=masini.pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        document.add(new Paragraph("Lista Mașinilor Tale"));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3);
        table.addCell("Marca");
        table.addCell("Model");
        table.addCell("Număr Înmatriculare");

        for (Car car : cars) {
            table.addCell(car.getBrand());
            table.addCell(car.getModel());
            table.addCell(car.getLicensePlate());
        }

        document.add(table);
        document.close();
    }

    @PostMapping("/dashboard/cars/{carId}/delete")
    public String deleteCar(@PathVariable int carId,
                            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Car car = carService.findById(carId);

        if (!car.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Nu ai permisiunea să ștergi această mașină.");
        }
        carService.deleteById(carId);
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/cars/{carId}/edit")
    public String updateCar(@PathVariable int carId,
                            @Valid @ModelAttribute("carDto") CarDto carDto,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        if (result.hasErrors()) {
            String email = userDetails.getUsername();
            List<Car> cars = carService.findByUserEmail(email);
            model.addAttribute("cars", cars);
            model.addAttribute("carDto", carDto);

            Map<Long, String> carStatus = new HashMap<>();
            for (Car car : cars) {
                String status = carService.calculateCarStatus(car);
                carStatus.put(car.getId(), status);
            }
            model.addAttribute("carStatus", carStatus);

            List<String> reminders = carDocumentService.generateRemindersForUser(email);
            model.addAttribute("reminders", reminders);
            return "dashboard";
        }
        carService.updateCar(carId, carDto, userDetails.getUsername());
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/cars/{carId}/documents/{documentId}/delete")
    public String deleteCarDocument(@PathVariable int carId,
                                    @PathVariable int documentId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Car car = carService.findById(carId);

        if (!car.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Nu ai permisiunea să ștergi acest document.");
        }

        carDocumentService.deleteById(documentId);

        return "redirect:/dashboard/cars/" + carId + "/documents";
    }

    @PostMapping("/dashboard/cars/{carId}/documents/{documentId}/editEndDate")
    public String updateDocumentExpiryDate(@PathVariable int carId,
                                           @PathVariable int documentId,
                                           @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate expiryDate,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Car car = carService.findById(carId);

        if (!car.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Nu ai permisiunea să editezi acest document.");
        }

        CarDocument document = carDocumentService.findById(documentId);
        document.setExpiryDate(expiryDate);
        carDocumentService.save(document);

        return "redirect:/dashboard/cars/" + carId + "/documents";
    }

}
