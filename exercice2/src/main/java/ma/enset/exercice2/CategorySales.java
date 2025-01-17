package ma.enset.exercice2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySales {
    private String category;
    private Double totalRevenue;
}
