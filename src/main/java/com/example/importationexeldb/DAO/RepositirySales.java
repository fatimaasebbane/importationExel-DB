package com.example.importationexeldb.DAO;

import com.example.importationexeldb.models.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Repository
public interface RepositirySales extends JpaRepository<Sales,Integer> {

    boolean existsById(String id);

}
