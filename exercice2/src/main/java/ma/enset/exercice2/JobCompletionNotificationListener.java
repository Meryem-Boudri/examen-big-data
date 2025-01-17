package ma.enset.exercice2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final JdbcTemplate jdbcTemplate;

    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            System.out.println("Le job s'est terminé avec succès !");
            displayResults();
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            System.out.println("Le job a échoué.");
        }
    }

    private void displayResults() {
        System.out.println("Résultats de la base de données :");

        String sql = "SELECT product, category, revenue, quantity_sold FROM sales_report";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        for (Map<String, Object> row : results) {
            System.out.println("Produit: " + row.get("product") +
                    ", Catégorie: " + row.get("category") +
                    ", Chiffre d'affaires: " + row.get("revenue") +
                    ", Quantité vendue: " + row.get("quantity_sold"));
        }

        // Requête pour afficher le nombre total de produits vendus par catégorie
        String sqlProductCount = "SELECT category, SUM(quantity_sold) AS total_products_sold FROM sales_report GROUP BY category";
        List<Map<String, Object>> productCountResults = jdbcTemplate.queryForList(sqlProductCount);

        System.out.println("\nNombre total de produits vendus par catégorie :");
        for (Map<String, Object> row : productCountResults) {
            System.out.println("Catégorie: " + row.get("category") + ", Nombre total de produits vendus: " + row.get("total_products_sold"));
        }
        // Affichage des résultats du chiffre d'affaires moyen par produit dans chaque catégorie
        String sqlAvgRevenue = "SELECT category, product, avg_revenue FROM category_product_avg_revenue";
        List<Map<String, Object>> avgRevenueResults = jdbcTemplate.queryForList(sqlAvgRevenue);

        System.out.println("Chiffre d'affaires moyen par produit par catégorie :");
        for (Map<String, Object> row : avgRevenueResults) {
            System.out.println("Catégorie: " + row.get("category") +
                    ", Produit: " + row.get("product") +
                    ", Chiffre d'affaires moyen: " + row.get("avg_revenue"));
        }

    }
}
