/*
 * La classe RandomDataSource rappresenta una sorgente di dati che genera casualmente 
 * linee di dati con un timestamp, utilizzando l'ID del camion fornito. Questo tipo di 
 * implementazione può essere utile per generare dati casuali allo scopo di stressare 
 * il sistema o simulare situazioni reali di flusso di dati.
 */

package it.unisannio.group8.sources;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class RandomDataSource implements DataSource {
    private final String truckId;
    private final Random random;
    
    private int counter;  // Contatore per il timestamp incrementale
    private static final double LATITUDE = 41.138388833;
    private static final double LONGITUDE = 14.7711125;

    // Il costruttore accetta l'ID del camion come argomento e 
    // lo memorizza per un utilizzo successivo
    public RandomDataSource(String truckId) {
        this.truckId = truckId;
        this.random = new Random(); 
        this.counter = 0;
    }

    @Override
    public void init() {}

    // Genera e restituisce una linea di dati in maniera casuale.
    @Override
    public String nextLine() {
    	// Genera l'ID del sacco casualmente
        String saccoId = generateRandomSaccoId();
        
        // Genera il timestamp in maniera casuale
        String timestamp = generateIncrementalTimestamp();
        
        
    	// Genera il timestamp in maniera casuale (modificando ora, minuti e secondi)
        /*int year = 2021;
        int month = 3;
        int day = 7;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);*/

        //String timestamp = String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);

        // Costruisci la linea di dati
        return String.format("%s, %s, TRUCK%s, 1, %s, %s", saccoId, timestamp, truckId, LATITUDE, LONGITUDE);
    }
    
    private String generateRandomSaccoId() {
    	// Genera l'ID del sacco casualmente (esempio)
        return "57434F4D503034303030313037" + String.format("%02d", random.nextInt(100));
    }
    
    private String generateIncrementalTimestamp() {
        // Incrementa il contatore di 3 secondi ad ogni chiamata
        counter += 3;

        // Utilizza LocalDateTime per gestire l'incremento di ore, minuti e secondi
        LocalDateTime dateTime = LocalDateTime.of(2021, 3, 7, 21, 26, 0).plusSeconds(counter);

        // Formatta la data in modo desiderato
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
