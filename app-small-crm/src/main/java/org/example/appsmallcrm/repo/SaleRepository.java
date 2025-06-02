package org.example.appsmallcrm.repo;

import org.example.appsmallcrm.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> { // JpaSpecificationExecutor can be added if complex queries on Sales are needed

    // Sum revenue within a date range
    @Query("SELECT SUM(s.revenue) FROM Sale s WHERE s.soldDate >= :startDate AND s.soldDate <= :endDate")
    Double sumRevenueBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Count sales within a date range
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.soldDate >= :startDate AND s.soldDate <= :endDate")
    Long countSalesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Average sales quantity (assuming quantity represents items sold in that sale instance)
    @Query("SELECT AVG(s.quantity) FROM Sale s WHERE s.soldDate >= :startDate AND s.soldDate <= :endDate")
    Double avgSalesQuantityBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    // Dashboard specific queries
    @Query("SELECT MONTH(s.soldDate) as month, SUM(s.revenue) as totalRevenue FROM Sale s WHERE YEAR(s.soldDate) = :year GROUP BY MONTH(s.soldDate) ORDER BY month ASC")
    List<Object[]> findMonthlySalesForYear(@Param("year") int year);

    @Query("SELECT s.soldDate as date, SUM(s.revenue) as totalRevenue FROM Sale s WHERE s.soldDate >= :startDate GROUP BY s.soldDate ORDER BY date ASC")
    List<Object[]> findDailySalesFrom(@Param("startDate") LocalDate startDate);

    // Revenue by product name
    @Query("SELECT p.name as productName, SUM(s.revenue) as totalRevenue FROM Sale s JOIN s.product p GROUP BY p.name")
    List<Object[]> findTotalRevenueByProductName();
}