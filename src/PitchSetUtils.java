import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PitchSetUtils {

    /**
     * Converte una stringa di pitch classes (separate da spazi, virgole, ecc.)
     * in una lista ordinata e unica di interi (0-11).
     * Supporta notazioni numeriche, 't' (per 10) ed 'e' (per 11).
     * Applica l'operazione modulo 12 per normalizzare i valori nel range 0-11.
     * Genera un'IllegalArgumentException se l'input contiene caratteri non validi o pitch classes numeriche fuori dal range 0-11.
     *
     * @param input La stringa contenente le pitch classes.
     * @return Una lista ordinata e unica di pitch classes.
     * @throws IllegalArgumentException Se l'input contiene caratteri diversi da numeri, 't', 'e', o se una pitch class numerica è fuori dal range 0-11.
     */
	
    public static List<Integer> parseAndCleanPitchClasses(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList(); // Non lanciamo errore qui, il chiamante (PitchSetCalculator) gestirà il set vuoto.
        }

        String normalizedInput = input.toLowerCase();

        // Validazione preliminare: controlla se ci sono caratteri non consentiti
        // Consentiti: numeri (0-9), 't', 'e', spazio, virgola, segno meno (temporaneamente per split, ma sarà gestito successivamente dopo)
        // Il pattern regex è stato modificato per non aspettarsi il '-' se non precede un numero per un parsing più pulito
        if (!normalizedInput.matches("^[0-9te,\\s-]+$")) { // Manteniamo '-' per lo split
             // Rileva se ci sono caratteri *veramente* invalidi non numeri, t, e, spazi, virgole, meno
            if (!normalizedInput.replaceAll("[0-9te,\\s-]", "").isEmpty()) {
                throw new IllegalArgumentException(
                    "Errore: L'input '" + input + "' contiene caratteri non validi. " +
                    "Sono ammessi solo numeri (0-11), 't' (per 10), 'e' (per 11), spazi e virgole."
                );
            }
        }
        
        // Lista temporanea per raccogliere pitch classes valide
        List<Integer> validPitchClasses = new ArrayList<>();
        StringBuilder errors = new StringBuilder(); // Per accumulare messaggi di errore specifici

        // Splitta l'input in token e processa
        String[] tokens = normalizedInput.split("[^0-9te-]+"); // Splitta per tutto ciò che NON è 0-9, t, e, -
        
        for (String token : tokens) {
            if (token.isEmpty() || token.equals("-")) { // Ignora stringhe vuote o solo "-" derivanti dallo split
                continue;
            }

            Integer pc = null;
            try {
                // Tenta di parsare 't' o 'e'
                if (token.equals("t")) {
                    pc = 10;
                } else if (token.equals("e")) {
                    pc = 11;
                } else {
                    // Tenta di parsare come numero intero
                    pc = Integer.parseInt(token);
                }

                // Ora, VALIDA il valore numerico
                if (pc != null) {
                    if (pc < 0 || pc > 11) {
                        errors.append("Errore: La pitch class '" + token + "' è fuori dal range consentito (0-11).\n");
                    } else {
                        validPitchClasses.add(pc);
                    }
                } else {
                    // Questo caso dovrebbe essere già coperto dal regex iniziale, ma è una safety net
                    errors.append("Errore: Il token '" + token + "' non è un numero valido, 't' o 'e'.\n");
                }

            } catch (NumberFormatException e) {
                // Questo catch ora cattura solo numeri malformati che il regex ha lasciato passare
                errors.append("Errore: Il token '" + token + "' non è un numero intero valido.\n");
            }
        }

        // Se ci sono stati errori specifici, lancia un'eccezione con tutti i messaggi
        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString().trim()); // Rimuove l'ultimo newline
        }
        
        // Ritorna la lista pulita, unica e ordinata
        return validPitchClasses.stream()
                                .distinct() // Rimuove duplicati
                                .sorted()   // Ordina in ordine crescente
                                .collect(Collectors.toList());
    }

    /**
     * Questo metodo non è più usato direttamente da parseAndCleanPitchClasses per la validazione,
     * ma è mantenuto per altri usi dove la normalizzazione modulo 12 è desiderata senza validazione del range.
     * Normalizza una pitch class assicurandone che sia nel range [0, 11] (modulo 12).
     * Gestisce correttamente sia numeri positivi che negativi.
     * Es: 12 -> 0, 13 -> 1, -1 -> 11.
     * @param pc La pitch class da normalizzare.
     * @return La pitch class normalizzata (0-11).
     */
    public static int normalizePc(int pc) {
        return (pc % 12 + 12) % 12;
    }

    // formatVector, transpose, invert, getRotations, getSuccessiveIntervals, getCircularIntervals, getSpan
    public static String toStringPcSet(List<Integer> arr) {
        if (arr == null || arr.isEmpty()) {
            return "<>";
        }
        return "<" + arr.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + ">";
    }

    public static List<Integer> transpose(List<Integer> pcSet, int n) {
        if (pcSet == null || pcSet.isEmpty()) {
            return Collections.emptyList();
        }
        int finalN = normalizePc(n);

        return pcSet.stream()
                .map(pc -> normalizePc(pc + finalN))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Calcola l'inversione (In) di un set di pitch classes.
     * Applica I_n: ogni pc_i diventa (n - pc_i) mod 12.
     * Il risultato viene poi invertito nell'ordine degli elementi,
     * e i duplicati vengono rimossi.
     *
     * @param pcSet Il set di pitch classes da invertire.
     * @param n L'asse di inversione (da 0 a 11).
     * @return Il set di pitch classes invertito e poi con ordine invertito, senza duplicati.
     */
    public static List<Integer> invert(List<Integer> pcSet, int n) {
        if (pcSet == null || pcSet.isEmpty()) {
            return Collections.emptyList();
        }
        int finalN = normalizePc(n);

        // 1. Applica l'inversione matematica (n - pc) mod 12 a ogni elemento
        List<Integer> invertedMapped = pcSet.stream()
                .map(pc -> normalizePc(finalN - pc))
                .collect(Collectors.toList()); // Raccoglie in una lista mantenendo l'ordine iniziale

        // 2. Inverti l'ordine della lista risultante
        Collections.reverse(invertedMapped);

        // 3. Rimuovi i duplicati e ordina (se desiderato per la rappresentazione finale del set)
        // Se si vuole mantenere l'ordine inverso e non riordinare, il .sorted() va rimosso.
        // Se si vuole rimuovere i duplicati, ma non ordinare, il .distinct() va mantenuto senza .sorted().
        // Date le tue istruzioni ("l'ultimo diventa il primo, il penultimo il secondo"),
        // assumeremo che non ci sia un riordinamento numerico dopo l'inversione dell'ordine.
        // La rimozione dei duplicati è ancora una scelta per un "set" o una "sequenza".
        // Per un "set", i duplicati si rimuovono. Per una "sequenza" i duplicati possono rimanere.
        // Manteniamo .distinct() per coerenza con l'idea di "set" ma rimuoviamo .sorted().

        return invertedMapped
        		.stream()
                .distinct() // Rimuovi i duplicati dopo l'inversione dell'ordine
                .collect(Collectors.toList());
    }

    public static List<List<Integer>> getRotations(List<Integer> sortedPcSet) {
        List<List<Integer>> rotations = new ArrayList<>();
        if (sortedPcSet == null || sortedPcSet.isEmpty()) {
            return rotations;
        }
        int n = sortedPcSet.size();
        if (n == 1) {
            rotations.add(new ArrayList<>(sortedPcSet));
            return rotations;
        }

        for (int i = 0; i < n; i++) {
            List<Integer> currentRot = new ArrayList<>();
            for (int k = 0; k < n; k++) {
                currentRot.add(sortedPcSet.get((i + k) % n));
            }
            rotations.add(currentRot);
        }
        return rotations;
    }

    public static List<Integer> getSuccessiveIntervals(List<Integer> pcs) {
        List<Integer> intervals = new ArrayList<>();
        if (pcs == null || pcs.size() < 2) {
            return intervals;
        }
        for (int i = 1; i < pcs.size(); i++) {
            intervals.add(normalizePc(pcs.get(i) - pcs.get(i - 1)));
        }
        return intervals;
    }

    public static List<Integer> getCircularIntervals(List<Integer> sortedPcs) {
        List<Integer> intervals = new ArrayList<>();
        if (sortedPcs == null || sortedPcs.isEmpty()) {
            return Collections.emptyList();
        }
        if (sortedPcs.size() == 1) {
            return Collections.emptyList();
        }

        for (int i = 0; i < sortedPcs.size() - 1; i++) {
            intervals.add(normalizePc(sortedPcs.get(i + 1) - sortedPcs.get(i)));
        }

        intervals.add(normalizePc(sortedPcs.get(0) + 12 - sortedPcs.get(sortedPcs.size() - 1)));

        return intervals;
    }

    public static int getSpan(List<Integer> pcs) {
        if (pcs == null || pcs.isEmpty() || pcs.size() == 1) {
            return 0;
        }
        return normalizePc(pcs.get(pcs.size() - 1) - pcs.get(0));
    }
}