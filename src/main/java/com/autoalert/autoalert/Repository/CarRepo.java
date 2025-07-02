package com.autoalert.autoalert.Repository;

import com.autoalert.autoalert.Model.Car;
import com.autoalert.autoalert.Model.CarDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepo extends JpaRepository<Car, Integer> {
    List<Car> findByUserEmail(String email);



}
