package ma.enset.exercice2;

import lombok.Data;

@Data
public class SalesReport {

    private Long id;

    private String product;
    private String category;
    private double revenue;
    private int quantitySold;
    private double averageRevenuePerProduct;

}
