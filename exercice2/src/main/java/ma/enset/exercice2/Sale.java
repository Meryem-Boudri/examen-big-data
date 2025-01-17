package ma.enset.exercice2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sale {
    private String transactionId;
    private String product;
    private String category;
    private int quantity;
    private double unitPrice;
    private LocalDate saleDate;

}
