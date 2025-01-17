package ma.enset;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

public class VehicleObstacleAlertApp {
    public static void main(String[] args) {
        // Configuration des propriétés Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "vehicle-obstacle-alerts");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Création du builder Kafka Streams
        StreamsBuilder builder = new StreamsBuilder();
        System.out.println("start");
        // Consommation des données du topic vehicle_data
        KStream<String, String> vehicleDataStream = builder.stream("vehicle-data", Consumed.with(Serdes.String(), Serdes.String()));

        // Filtrage des véhicules avec distance au prochain obstacle inférieure à 5 mètres
        vehicleDataStream.filter((key, value) -> {
                    System.out.println("filter");
                    String[] parts = value.split("\\|");
                    double distanceToNextObstacle = Double.parseDouble(parts[4]); // Distance à l'obstacle
                    return distanceToNextObstacle < 5.0; // Seuil critique = 5 mètres
                })
                .mapValues(value -> {
                    System.out.println("mapValue");
                    String[] parts = value.split("\\|");
                    return parts[0] + "|" + parts[4] + "|Obstacle too close!";
                    // Message d'alerte formaté
                })
                .to("obstacle-alerts", Produced.with(Serdes.String(), Serdes.String())); // Envoi de l'alerte au topic obstacle_alerts
        System.out.println("done");
        // Démarrage de l'application Kafka Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
