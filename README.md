# **Projet Big Data**

## **Description**

Ce projet contient deux solutions principales :

1. **Traitement des données des patients hospitalisés avec Spring Batch**.
2. **Surveillance des incidents hospitaliers avec Kafka Streams**.

---

## **Exercice 1 : Spring Batch**

### **Objectif**
- Lire les données des patients depuis un fichier CSV.
- Calculer la durée moyenne des séjours par service.
- Stocker les résultats dans une base de données H2 en mémoire.

### **Étapes**
1. **Lecture des données** : Utilisation de `FlatFileItemReader` pour lire le fichier `hospitalizations.csv`.
2. **Traitement des données** :
   - Calcul de la durée de séjour pour chaque patient.
   - Exclusion des enregistrements avec une date de sortie manquante.
3. **Écriture des résultats** : Utilisation de `JdbcBatchItemWriter` pour insérer les résultats dans la base de données.
4. **Planification** : Le job est exécuté automatiquement tous les jours à minuit grâce à `@EnableScheduling`.

### **Fichier CSV attendu**
```csv
ID,Nom,Service,DateAdmission,DateSortie
1,Ahmed Saidi,Cardiologie,2024-12-01,2024-12-05
2,Hajar Tazi,Oncologie,2024-12-02,2024-12-10
3,Said Alami,Cardiologie,2024-12-03,2024-12-06
```

### **Résultats**
Les durées moyennes par service sont stockées dans une table H2 nommée `service_stats`.


---

## **Exercice 2 : Kafka Streams**

### **Objectif**
- Lire les données des incidents hospitaliers depuis un topic Kafka nommé `hospital_incidents`.
- Filtrer les incidents critiques.
- Agréger les résultats par service et type d'incident.
- Produire un rapport dans le topic Kafka `incident_report`.

### **Étapes**
1. **Configuration Kafka Streams** :
   - Les propriétés incluent `application.id` et `bootstrap.servers`.
2. **Filtrage des incidents critiques** :
   - Critère : `severity = "Critique"`.
3. **Agrégation et publication** :
   - Classement par service et type.
   - Calcul du total des incidents critiques par catégorie.
   - Publication dans le topic `incident_report`.

### **Modification additionnelle**
- Calcul de la durée moyenne entre deux incidents critiques par service.
- Publication des résultats dans le topic `incident_time_analysis`.

### **Commande pour exécuter**
1. Créer les topics :
   ```bash
   kafka-topics --create --topic hospital_incidents --bootstrap-server localhost:9092
   kafka-topics --create --topic incident_report --bootstrap-server localhost:9092
   kafka-topics --create --topic incident_time_analysis --bootstrap-server localhost:9092
   ```

---

## **Configuration de la Base de Données H2**

- **URL d'accès** : `jdbc:h2:mem:testdb`
- **Utilisateur** : `sa`
- **Mot de passe** : `password`

## **Dépendances**
- **Spring Boot** : pour Spring Batch et la gestion de la base de données.
- **Apache Kafka** : pour Kafka Streams.


---

## **Contributeurs**
- **Meryem Boudri**

**Année universitaire : 2024/2025**
