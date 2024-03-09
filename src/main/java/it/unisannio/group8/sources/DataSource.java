package it.unisannio.group8.sources;

public interface DataSource {
	// Responsabile dell'inizializzazione delle risorse necessarie per la sorgente di dati
	public void init();
	// Restituisce una stringa rappresentante la prossima linea di dati dalla sorgente
	public String nextLine();
}
