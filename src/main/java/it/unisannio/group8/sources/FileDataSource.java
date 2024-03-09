/*
 * La classe FileDataSource rappresenta una sorgente di dati che legge sequenzialmente 
 * le linee da un file. Il metodo init() inizializza la sorgente aprendo il file, 
 * mentre il metodo nextLine() restituisce la prossima linea di dati ad ogni chiamata. 
 * Questo tipo di implementazione può essere utilizzato quando si desidera leggere dati 
 * da un file specifico come parte del flusso di dati del sistema.
 * 
 */

package it.unisannio.group8.sources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileDataSource implements DataSource {
    private BufferedReader reader;
    private final String filePath;
    
    // Il costruttore accetta il percorso del file come argomento e lo memorizza
    public FileDataSource(String filePath) {
        this.filePath = filePath;
    }
    
    // Inizializza la sorgente di dati
    @Override
    public void init() {
        try {
        	// Apre il file specificato nel costruttore e crea un oggetto BufferedReader per leggere il contenuto del file
            reader = new BufferedReader(new FileReader(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Error opening file: " + filePath, e);
        }
    }
    
    // Restituisce la prossima linea di dati dal file
    @Override
    public String nextLine() {
        try {
        	// Utilizza il BufferedReader per leggere la prossima linea dal file
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Error reading line from file: " + filePath, e);
        }
    }
}
