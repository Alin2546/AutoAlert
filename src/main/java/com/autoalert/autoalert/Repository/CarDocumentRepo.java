package com.autoalert.autoalert.Repository;

import com.autoalert.autoalert.Model.Car;
import com.autoalert.autoalert.Model.CarDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CarDocumentRepo extends JpaRepository<CarDocument, Integer> {
    List<CarDocument> findByExpiryDate(LocalDate expiryDate);
    List<CarDocument> findByCarId(int carId);


}
