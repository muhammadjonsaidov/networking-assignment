package org.example.appsmallcrm.entity;

import lombok.Data;

@Data

public class DashboardStats {

    private int totalProduct;
    private double productRevenue;
    private int productSold;
    private double avgMonthlySales;
    private double revenueChange;
    private double soldChange;
    private double avgSalesChange;
}