package ma.enset.exercice2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Configuration
public class BatchConfig {
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfig.class);

    // Example logging for bean initialization
    public BatchConfig() {
        LOGGER.info("BatchConfig initialized");
    }

    // Read from CSV file
    @Bean
    public FlatFileItemReader<Sale> reader() throws Exception {
        LOGGER.info("Read successfully");

        BeanWrapperFieldSetMapper<Sale> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Sale.class);
        fieldSetMapper.setCustomEditors(Map.of(
                LocalDate.class, new PropertyEditorSupport() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    @Override
                    public void setAsText(String text) {
                        setValue(LocalDate.parse(text, formatter));
                    }
                }
        ));

        return new FlatFileItemReaderBuilder<Sale>()
                .name("saleItemReader")
                .resource(new ClassPathResource("sales.csv"))
                .delimited()
                .names("transactionId", "product", "category", "quantity", "unitPrice", "saleDate")
                .linesToSkip(1)
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

    @Bean
    public ItemProcessor<Sale, SalesReport> itemProcessor() {
        return sale -> {
            if (sale.getQuantity() <= 0 || sale.getUnitPrice() <= 0) {
                return null; // Ignorer les enregistrements invalides
            }
            SalesReport report = new SalesReport();
            report.setProduct(sale.getProduct());
            report.setCategory(sale.getCategory());
            report.setRevenue(sale.getQuantity() * sale.getUnitPrice());
            report.setQuantitySold(sale.getQuantity());
            return report;
        };
    }


    // Writing Service stats (AVG duration per service) into service_stats table
    // ItemWriter avec JdbcBatchItemWriterBuilder
    @Bean
    public ItemWriter<SalesReport> itemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<SalesReport>()
                .dataSource(dataSource)
                .sql("INSERT INTO sales_report (product, category, revenue, quantity_sold) " +
                        "VALUES (:product, :category, :revenue, :quantitySold)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step processSalesStep(JobRepository jobRepository,
                                 DataSourceTransactionManager transactionManager,
                                 ItemReader<Sale> itemReader,
                                 ItemProcessor<Sale, SalesReport> itemProcessor,
                                 ItemWriter<SalesReport> itemWriter) {
        return new StepBuilder("processSalesStep",jobRepository)
                .<Sale, SalesReport>chunk(10,transactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public Step step2(JobRepository jobRepository,
                      DataSourceTransactionManager transactionManager,
                      JdbcTemplate jdbcTemplate) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            List<CategorySales> categorySales = jdbcTemplate.query(
                    "SELECT category, SUM(revenue) AS total_revenue FROM sales_report GROUP BY category",
                    (rs, rowNum) -> new CategorySales(
                            rs.getString("category"),
                            rs.getDouble("total_revenue")
                    )
            );

            for (CategorySales category : categorySales) {
                jdbcTemplate.update(
                        "INSERT INTO category_sales (category, total_revenue) VALUES (?, ?)",
                        category.getCategory(),
                        category.getTotalRevenue()
                );
            }
            return RepeatStatus.FINISHED;

        };
        return new StepBuilder("step2", jobRepository)
                .tasklet(tasklet, transactionManager) // Set tasklet and transaction manager
                .build();

    }
    // Nouvelle étape pour calculer le chiffre d'affaires moyen par produit dans chaque catégorie
    @Bean
    public Step step3(JobRepository jobRepository,
                      DataSourceTransactionManager transactionManager,
                      JdbcTemplate jdbcTemplate) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            // Calcul du chiffre d'affaires moyen par produit dans chaque catégorie
            String sql = "SELECT category, product, AVG(revenue) AS avg_revenue " +
                    "FROM sales_report GROUP BY category, product";

            List<Map<String, Object>> avgRevenueResults = jdbcTemplate.queryForList(sql);

            // Insertion des résultats dans une table (par exemple category_product_avg_revenue)
            for (Map<String, Object> row : avgRevenueResults) {
                String category = (String) row.get("category");
                String product = (String) row.get("product");

                // Récupération du chiffre d'affaires moyen sous forme de BigDecimal et conversion en Double
                BigDecimal avgRevenueBigDecimal = (BigDecimal) row.get("avg_revenue");
                Double avgRevenue = avgRevenueBigDecimal != null ? avgRevenueBigDecimal.doubleValue() : null;

                // Insertion dans la base de données
                jdbcTemplate.update(
                        "INSERT INTO category_product_avg_revenue (category, product, avg_revenue) " +
                                "VALUES (?, ?, ?)", category, product, avgRevenue);
            }
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("step3", jobRepository)
                .tasklet(tasklet, transactionManager) // Affecter le tasklet et le gestionnaire de transactions
                .build();
    }

    @Bean
    public Job salesJob(JobRepository jobRepository,
                                        Step processSalesStep,
                                        Step step2,
                                        Step step3,
                                        JobCompletionNotificationListener listener) {
        return new JobBuilder("calculateStayDurationJob", jobRepository)
                .listener(listener)
                .start(processSalesStep)
                .next(step2)   // Calcul du revenu total par catégorie
                .next(step3)   // Calcul du chiffre d'affaires moyen par produit
                .build();
    }
}
