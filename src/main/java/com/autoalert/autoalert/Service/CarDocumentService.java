package com.autoalert.autoalert.Service;

import com.autoalert.autoalert.Model.Car;
import com.autoalert.autoalert.Model.CarDocument;
import com.autoalert.autoalert.Model.DocumentType;
import com.autoalert.autoalert.Model.Dto.CarDocumentDto;
import com.autoalert.autoalert.Repository.CarDocumentRepo;
import com.autoalert.autoalert.Repository.CarRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarDocumentService {

    private final CarDocumentRepo carDocumentRepository;
    private final CarRepo carRepository;

    public List<CarDocument> findByCarId(int carId) {
        return carDocumentRepository.findByCarId(carId);
    }

    public void addDocumentToCar(int carId, CarDocumentDto carDocumentDto) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Mașina nu a fost găsită cu id: " + carId));

        CarDocument doc = new CarDocument();
        doc.setDocumentType(carDocumentDto.getDocumentType());
        doc.setExpiryDate(carDocumentDto.getExpiryDate());
        doc.setCar(car);
        carDocumentRepository.save(doc);
    }

    public List<String> generateRemindersForUser(String userEmail) {
        List<Car> cars = carRepository.findByUserEmail(userEmail);
        List<String> reminders = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(7);

        for (Car car : cars) {
            for (CarDocument doc : car.getDocuments()) {
                if (doc.getExpiryDate() != null) {
                    LocalDate expiry = doc.getExpiryDate();
                    if (!expiry.isBefore(today) && !expiry.isAfter(thresholdDate)) {
                        String reminder = String.format(
                                "Documentul %s pentru mașina %s %s expiră pe %s",
                                doc.getDocumentType(),
                                car.getBrand(),
                                car.getModel(),
                                expiry
                        );
                        reminders.add(reminder);
                    }
                }
            }
        }
        return reminders;
    }

    public List<CarDocument> findDocumentsByExpiryDate(LocalDate expiryDate) {
        return carDocumentRepository.findByExpiryDate(expiryDate);
    }

    public void deleteById(int documentId) {
        carDocumentRepository.deleteById(documentId);
    }

    public CarDocument findById(int documentId) {
        Optional<CarDocument> optionalDoc = carDocumentRepository.findById(documentId);
        return optionalDoc.orElseThrow(() -> new RuntimeException("Documentul nu a fost găsit cu id: " + documentId));
    }

    public void save(CarDocument document) {
        carDocumentRepository.save(document);
    }
}


