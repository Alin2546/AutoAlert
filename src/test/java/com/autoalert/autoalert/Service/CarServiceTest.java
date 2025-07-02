package com.autoalert.autoalert.Service;

import com.autoalert.autoalert.Model.Car;
import com.autoalert.autoalert.Model.CarDocument;
import com.autoalert.autoalert.Model.Dto.CarDto;
import com.autoalert.autoalert.Model.User;
import com.autoalert.autoalert.Repository.CarRepo;
import com.autoalert.autoalert.Repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CarServiceTest {

    @Autowired
    private CarRepo carRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CarService carService;

    @Test
    public void testAddCarToUser_NoInput() {
        assertThrows(RuntimeException.class, () -> carService.addCarToUser(null, null));
    }

    @Test
    public void testAddCarToUser_WrongInput_UserNotFound() {
        CarDto carDto = new CarDto();
        carDto.setBrand("Dacia");
        carDto.setModel("Logan");
        carDto.setLicensePlate("IS-01-XYZ");

        assertThrows(RuntimeException.class, () -> carService.addCarToUser(carDto, "ghost@email.com"));
    }
    @Test
    public void testAddCarToUser_OneValidCar() {
        User user = new User();
        user.setEmail("car@test.com");
        user.setPassword("123");
        userRepo.save(user);

        CarDto carDto = new CarDto();
        carDto.setBrand("Tesla");
        carDto.setModel("Model 3");
        carDto.setLicensePlate("B-99-XYZ");

        carService.addCarToUser(carDto, user.getEmail());

        List<Car> cars = carRepo.findAll();
        assertEquals(1, cars.size());
        assertEquals(user.getId(), cars.get(0).getUser().getId());
    }
    @Test
    public void testAddCarToUser_TwoCarsForSameUser() {
        User user = new User();
        user.setEmail("twocars@test.com");
        user.setPassword("abc");
        userRepo.save(user);

        CarDto car1 = new CarDto();
        car1.setBrand("Ford");
        car1.setModel("Focus");
        car1.setLicensePlate("IS-11-ABC");

        CarDto car2 = new CarDto();
        car2.setBrand("VW");
        car2.setModel("Golf");
        car2.setLicensePlate("IS-22-DEF");

        carService.addCarToUser(car1, user.getEmail());
        carService.addCarToUser(car2, user.getEmail());

        List<Car> cars = carRepo.findAll();
        assertEquals(2, cars.size());
        assertTrue(cars.stream().allMatch(c -> c.getUser().getEmail().equals("twocars@test.com")));
    }
    @Test
    public void testUpdateCar_NoInput() {
        assertThrows(RuntimeException.class, () -> carService.updateCar(0, null, null));
    }
    @Test
    public void testUpdateCar_WrongUserEmail_ThrowsException() {
        User user = new User();
        user.setEmail("owner@test.com");
        user.setPassword("123");
        userRepo.save(user);

        Car car = new Car();
        car.setBrand("BMW");
        car.setModel("X5");
        car.setLicensePlate("IS-00-XYZ");
        car.setUser(user);
        carRepo.save(car);

        CarDto carDto = new CarDto();
        carDto.setBrand("Audi");
        carDto.setModel("A6");
        carDto.setLicensePlate("IS-99-AAA");

        assertThrows(RuntimeException.class, () -> carService.updateCar(Math.toIntExact(car.getId()), carDto, "intrus@test.com"));
    }

    @Test
    public void testUpdateCar_OneValidInput() {
        User user = new User();
        user.setEmail("valid@test.com");
        user.setPassword("123");
        userRepo.save(user);

        Car car = new Car();
        car.setBrand("Ford");
        car.setModel("Fiesta");
        car.setLicensePlate("IS-10-BBB");
        car.setUser(user);
        carRepo.save(car);

        CarDto carDto = new CarDto();
        carDto.setBrand("Ford");
        carDto.setModel("Focus");
        carDto.setLicensePlate("IS-11-CCC");

        carService.updateCar(Math.toIntExact(car.getId()), carDto, user.getEmail());

        Car updated = carRepo.findById(Math.toIntExact(car.getId())).orElse(null);
        assertNotNull(updated);
        assertEquals("Focus", updated.getModel());
        assertEquals("IS-11-CCC", updated.getLicensePlate());
    }

    @Test
    public void testUpdateCar_TwoValidInputs() {
        User user = new User();
        user.setEmail("dual@test.com");
        user.setPassword("abc");
        userRepo.save(user);

        Car car1 = new Car();
        car1.setBrand("Skoda");
        car1.setModel("Octavia");
        car1.setLicensePlate("IS-01-SKO");
        car1.setUser(user);

        Car car2 = new Car();
        car2.setBrand("Mazda");
        car2.setModel("CX-5");
        car2.setLicensePlate("IS-02-MAZ");
        car2.setUser(user);

        carRepo.saveAll(List.of(car1, car2));

        CarDto dto1 = new CarDto();
        dto1.setBrand("Skoda");
        dto1.setModel("Superb");
        dto1.setLicensePlate("IS-01-NEW");

        CarDto dto2 = new CarDto();
        dto2.setBrand("Mazda");
        dto2.setModel("6");
        dto2.setLicensePlate("IS-02-NEW");

        carService.updateCar(Math.toIntExact(car1.getId()), dto1, user.getEmail());
        carService.updateCar(Math.toIntExact(car2.getId()), dto2, user.getEmail());

        assertEquals("Superb", carRepo.findById(Math.toIntExact(car1.getId())).get().getModel());
        assertEquals("6", carRepo.findById(Math.toIntExact(car2.getId())).get().getModel());
    }

    @Test
    public void testCalculateCarStatus_NoInput() {
        assertThrows(NullPointerException.class, () -> carService.calculateCarStatus(null));
    }
    @Test
    public void testCalculateCarStatus_WrongInput_EmptyDocuments() {
        Car car = new Car();
        car.setDocuments(new ArrayList<>());

        String status = carService.calculateCarStatus(car);
        assertEquals("valid", status);
    }
    @Test
    public void testCalculateCarStatus_ExpiredDocument() {
        Car car = new Car();
        CarDocument doc = new CarDocument();
        doc.setExpiryDate(LocalDate.now().minusDays(1));
        car.setDocuments(List.of(doc));

        String status = carService.calculateCarStatus(car);
        assertEquals("expired", status);
    }

    @Test
    public void testCalculateCarStatus_WarningDocument() {
        Car car = new Car();

        CarDocument validDoc = new CarDocument();
        validDoc.setExpiryDate(LocalDate.now().plusDays(30));

        CarDocument warningDoc = new CarDocument();
        warningDoc.setExpiryDate(LocalDate.now().plusDays(3));

        car.setDocuments(List.of(validDoc, warningDoc));

        String status = carService.calculateCarStatus(car);
        assertEquals("warning", status);
    }

    @Test
    public void testCalculateCarStatus_AllValid() {
        Car car = new Car();

        CarDocument doc1 = new CarDocument();
        doc1.setExpiryDate(LocalDate.now().plusDays(10));

        CarDocument doc2 = new CarDocument();
        doc2.setExpiryDate(LocalDate.now().plusDays(100));

        car.setDocuments(List.of(doc1, doc2));

        String status = carService.calculateCarStatus(car);
        assertEquals("valid", status);
    }

}

