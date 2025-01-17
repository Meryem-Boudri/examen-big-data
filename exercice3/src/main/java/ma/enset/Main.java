package ma.enset;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.StreamsBuilder;

import java.util.Properties;

public class Main {
    public static void main(String[] args) {

        // Configuration des propriétés Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "vehicle-speed-alerts");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Création du builder Kafka Streams
        StreamsBuilder builder = new StreamsBuilder();

        // Consommation des données du topic vehicle_data
        KStream<String, String> vehicleDataStream = builder.stream("vehicle-data", Consumed.with(Serdes.String(), Serdes.String()));
        // Filtrage des véhicules en excès de vitesse
        vehicleDataStream.filter((key, value) -> {


                    String[] parts = value.split("\\|");
                    int speed = Integer.parseInt(parts[1]); // Vitesse
                    return speed > 80; // Vitesse autorisée = 80 km/h
                })
                .mapValues(value -> {
                    String[] parts = value.split("\\|");
                    return parts[0] + "|" + parts[1] + "|" + parts[2] + "|" + parts[3] + "|Overspeeding|" + parts[5];
                    // Message d'alerte formaté
                })
                .to("speed-alerts", Produced.with(Serdes.String(), Serdes.String())); // Envoi de l'alerte au topic speed_alerts

        // Démarrage de l'application Kafka Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
