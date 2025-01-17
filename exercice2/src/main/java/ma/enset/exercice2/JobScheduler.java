package ma.enset.exercice2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JobScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

    private JobLauncher jobLauncher;
    private Job calculateStayDurationJob;

    public JobScheduler(JobLauncher jobLauncher, Job importOrderJob) {
        this.jobLauncher = jobLauncher;
        this.calculateStayDurationJob = importOrderJob;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Exécution à minuit tous les jours

    public void runJob(){
        try {
            // Créez un paramètre unique pour chaque exécution du job
            String uniqueJobParameter = UUID.randomUUID().toString();

            // Construire les paramètres du job en ajoutant un paramètre unique à chaque exécution
            JobParameters params = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis()) // Timestamp actuel
                    .addString("jobId", uniqueJobParameter) // Paramètre unique pour éviter la duplication
                    .toJobParameters();

            jobLauncher.run(calculateStayDurationJob,params);
            LOGGER.info("job completed");
        }
        catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }
}
