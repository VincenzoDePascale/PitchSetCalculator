import java.util.List;
import java.util.Scanner;

public class PitchSetCalculator {

    /**
     * Esegue il loop principale del programma, gestendo l'input utente e il menu delle operazioni.
     *
     * @param args Gli argomenti da riga di comando passati all'applicazione.
     */
    public void run(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputLine = "";
        List<Integer> currentPcSet = null; // Memorizza l'insieme iniziale per operazioni ripetute

        // Ciclo principale per un'operatività continua
        while (true) {
            // Ottieni l'input iniziale del set di pitch classes
            if (args.length > 0 && currentPcSet == null) { // Usa gli argomenti solo per il primissimo input, se forniti
                inputLine = String.join(" ", args);
            } else { // Chiede un nuovo input se non ci sono argomenti o se l'utente vuole inserire un nuovo set
                if (currentPcSet != null) { // Se esiste un set, chiede se si vuole inserirne uno nuovo
                    System.out.println("\nVuoi analizzare un nuovo insieme? (s/n)");
                    String newSetChoice = scanner.nextLine().trim().toLowerCase();
                    if (!newSetChoice.equals("s")) {
                        // Se non è 's', continua con il set corrente per ulteriori operazioni nel ciclo interno
                        inputLine = ""; // Pulisce inputLine per evitare di ri-analizzare il vecchio set inutilmente
                    } else {
                        System.out.println("Inserisci le nuove pitch classes:");
                        inputLine = scanner.nextLine();
                    }
                } else { // Prima esecuzione o dopo aver scelto '4. Inserisci un nuovo insieme'
                    System.out.println("Inserisci le pitch classes (0-11, 't' per 10, 'e' per 11) separate da spazi o virgole:");
                    inputLine = scanner.nextLine();
                }
            }

            // Tenta di fare il parsing solo se inputLine non è vuota
            if (!inputLine.isEmpty()) {
                try {
                    currentPcSet = PitchSetUtils.parseAndCleanPitchClasses(inputLine);
                } catch (IllegalArgumentException e) {
                    System.err.println(e.getMessage());
                    args = new String[0]; // Resetta gli argomenti per forzare l'input interattivo nel prossimo ciclo
                    continue; // Riavvia il ciclo per un nuovo input
                }
            } else if (currentPcSet == null) { // Caso in cui inputLine è vuota E currentPcSet è null (input iniziale vuoto)
                System.err.println("Errore: Nessun numero valido inserito o set vuoto dopo il parsing. Riprova.");
                args = new String[0]; // Resetta gli argomenti per forzare l'input interattivo nel prossimo ciclo
                continue; // Riavvia il ciclo per un nuovo input
            }

            if (currentPcSet.isEmpty()) {
                System.err.println("Errore: Nessun numero valido inserito o set vuoto dopo il parsing. Riprova.");
                args = new String[0]; // Resetta gli argomenti per forzare l'input interattivo nel prossimo ciclo
                continue; // Riavvia il ciclo per un nuovo input
            }

            // Mostra i risultati iniziali dell'analisi per il set corrente
            System.out.println("\n--- Risultati Analisi Insieme Iniziale ---");
            System.out.println("Input PCs:        " + PitchSetUtils.toStringPcSet(currentPcSet));
            RotationDetails normObj = PitchSetAnalyzer.getNormalOrderObject(currentPcSet);
            List<Integer> primeForm = PitchSetAnalyzer.getPrimeForm(currentPcSet);
            List<Integer> pfIntervals = PitchSetUtils.getSuccessiveIntervals(primeForm);

            System.out.println("Normal Form:      " + PitchSetUtils.toStringPcSet(normObj.getRotation()));
            System.out.println("Primary Form:     " + PitchSetUtils.toStringPcSet(primeForm));
            System.out.println("Intervalli PF:    " + PitchSetUtils.toStringPcSet(pfIntervals));

            // Ciclo del menu per le operazioni sul set corrente
            while (true) {
                System.out.println("\n--- Scegli un'operazione ---");
                System.out.println("1. Mostra tutte le trasposizioni e inversioni (T & I)");
                System.out.println("2. Trasporta l'insieme (T_n)");
                System.out.println("3. Inverti l'insieme (I_n)");
                System.out.println("4. Inserisci un nuovo insieme");
                System.out.println("5. Esci");
                System.out.print("La tua scelta: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        displayAllTranspositionsAndInversions(currentPcSet);
                        break;
                    case "2":
                        transposeSet(currentPcSet, scanner);
                        break;
                    case "3":
                        invertSet(currentPcSet, scanner);
                        break;
                    case "4":
                        currentPcSet = null; // Flag per chiedere un nuovo input nel ciclo principale
                        break; // Esce dal ciclo interno per tornare al prompt di input principale
                    case "5":
                        System.out.println("Uscita dal programma. Arrivederci!");
                        scanner.close();
                        return; // Esce dall'applicazione
                    default:
                        System.out.println("Scelta non valida. Riprova.");
                }

                if (currentPcSet == null) { // Se è stato scelto '4', esce da questo ciclo interno
                    break;
                }
            }
            args = new String[0]; // Pulisce gli argomenti da riga di comando dopo il primo uso
        }
    }

    /**
     * Mostra tutte le 12 trasposizioni di un set e le 12 inversioni in formato tabellare.
     * @param pcSet Il set di pitch classes di partenza.
     */
    private static void displayAllTranspositionsAndInversions(List<Integer> pcSet) {
        System.out.println("\n--- Categorie T & I ---");

        // Le stringhe formattate delle trasposizioni e inversioni
        String[] tnStrings = new String[12];
        String[] inStrings = new String[12];
        int maxTnStringLength = 0;
        int maxInStringLength = 0;

        for (int n = 0; n < 12; n++) {
            List<Integer> transposedSet = PitchSetUtils.transpose(pcSet, n);
            tnStrings[n] = PitchSetUtils.toStringPcSet(transposedSet);
            maxTnStringLength = Math.max(maxTnStringLength, tnStrings[n].length());

            List<Integer> invertedSet = PitchSetUtils.invert(pcSet, n);
            inStrings[n] = PitchSetUtils.toStringPcSet(invertedSet);
            maxInStringLength = Math.max(maxInStringLength, inStrings[n].length());
        }

        // Assicura una larghezza minima per leggibilità, se i set sono molto piccoli
        maxTnStringLength = Math.max(maxTnStringLength, 5); // Es. "<0>"
        maxInStringLength = Math.max(maxInStringLength, 5);

        // Intestazioni delle colonne
        System.out.printf("%-4s | %-" + maxTnStringLength + "s | %-4s | %-" + maxInStringLength + "s%n",
                          "n", "Tn", "n", "In");
        System.out.printf("-----|-%-" + maxTnStringLength + "s-|-----|-%-" + maxInStringLength + "s%n",
                          repeatChar('-', maxTnStringLength), repeatChar('-', maxInStringLength));

        // Stampa le righe della tabella
        for (int n = 0; n < 12; n++) {
            System.out.printf("%-4d | %-" + maxTnStringLength + "s | %-4d | %-" + maxInStringLength + "s%n",
                              n, tnStrings[n], n, inStrings[n]);
        }
    }

    /**
     * Metodo di supporto per ripetere un carattere per la formattazione.
     * @param charToRepeat Il carattere da ripetere.
     * @param count Quante volte ripetere.
     * @return Una stringa con il carattere ripetuto.
     */
    private static String repeatChar(char charToRepeat, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(charToRepeat);
        }
        return sb.toString();
    }

    /**
     * Permette all'utente di specificare un intervallo di trasporto e mostra il set risultante.
     * @param pcSet Il set di pitch classes di partenza.
     * @param scanner Lo scanner per l'input utente.
     */
    private static void transposeSet(List<Integer> pcSet, Scanner scanner) {
        System.out.print("Inserisci l'intervallo di trasporto (n da 0 a 11): ");
        try {
            int n = Integer.parseInt(scanner.nextLine().trim());
            if (n >= 0 && n <= 11) {
                List<Integer> transposedSet = PitchSetUtils.transpose(pcSet, n);
                System.out.println("T_" + n + " di " + PitchSetUtils.toStringPcSet(pcSet) + " è: " + PitchSetUtils.toStringPcSet(transposedSet));
            } else {
                System.out.println("Intervallo non valido. Inserisci un numero tra 0 e 11.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non numerico. Riprova.");
        }
    }

    /**
     * Permette all'utente di specificare un asse di inversione e mostra il set risultante.
     * @param pcSet Il set di pitch classes di partenza.
     * @param scanner Lo scanner per l'input utente.
     */
    private static void invertSet(List<Integer> pcSet, Scanner scanner) {
        System.out.print("Inserisci l'asse di inversione (n da 0 a 11): ");
        try {
            int n = Integer.parseInt(scanner.nextLine().trim());
            if (n >= 0 && n <= 11) {
                List<Integer> invertedSet = PitchSetUtils.invert(pcSet, n);
                System.out.println("I_" + n + " di " + PitchSetUtils.toStringPcSet(pcSet) + " è: " + PitchSetUtils.toStringPcSet(invertedSet));
            } else {
                System.out.println("Asse non valido. Inserisci un numero tra 0 e 11.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non numerico. Riprova.");
        }
    }
}