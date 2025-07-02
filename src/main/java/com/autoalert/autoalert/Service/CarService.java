package com.autoalert.autoalert.Service;

import com.autoalert.autoalert.Model.Car;
import com.autoalert.autoalert.Model.Dto.CarDto;
import com.autoalert.autoalert.Model.User;
import com.autoalert.autoalert.Repository.CarRepo;
import com.autoalert.autoalert.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepo carRepository;
    private final UserRepo userRepository;


    public List<Car> findByUserEmail(String email) {
        return carRepository.findByUserEmail(email);
    }

    public Car findById(int id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mașina nu a fost găsită cu id: " + id));
    }

    public void deleteById(int id) {
        carRepository.deleteById(id);
    }

    public void updateCar(int carId, CarDto carDto, String userEmail) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Mașina nu a fost găsită cu id: " + carId));

        if (!car.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Nu ai permisiunea să editezi această mașină.");
        }

        car.setBrand(carDto.getBrand());
        car.setModel(carDto.getModel());
        car.setLicensePlate(carDto.getLicensePlate());
        carRepository.save(car);
    }


    public String calculateCarStatus(Car car) {
        LocalDate today = LocalDate.now();

        boolean hasExpired = car.getDocuments().stream()
                .anyMatch(doc -> doc.getExpiryDate() != null && doc.getExpiryDate().isBefore(today));

        boolean hasWarning = car.getDocuments().stream()
                .anyMatch(doc -> {
                    if (doc.getExpiryDate() == null) return false;
                    LocalDate expiry = doc.getExpiryDate();
                    LocalDate warningThreshold = today.plusDays(7);
                    return !expiry.isBefore(today) && !expiry.isAfter(warningThreshold);
                });

        if (hasExpired) {
            return "expired";
        } else if (hasWarning) {
            return "warning";
        } else {
            return "valid";
        }
    }


    public void addCarToUser(CarDto carDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit: " + userEmail));

        Car car = new Car();
        car.setModel(carDto.getModel());
        car.setBrand(carDto.getBrand());
        car.setLicensePlate(carDto.getLicensePlate());
        car.setUser(user);
        carRepository.save(car);
    }
}
