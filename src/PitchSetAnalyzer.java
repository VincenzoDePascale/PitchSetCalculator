import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PitchSetAnalyzer {

    /**
     * Calcola l'oggetto RotationDetails che rappresenta l'Ordine Normale (Normal Order)
     * di un dato set di pitch classes.
     * Questa implementazione segue la definizione accademica rigorosa, valutando tutte le
     * rotazioni per trovare quella con il minimo span e la massima "left-packedness".
     *
     * @param pcSet L'insieme di pitch classes di input (non necessariamente ordinato o pulito).
     * @return Un oggetto RotationDetails contenente la rotazione dell'ordine normale (trasposta a 0),
     * il suo span e gli intervalli successivi.
     */
    public static RotationDetails getNormalOrderObject(List<Integer> pcSet) {
        // 1. Pulisci e ordina l'input.
        List<Integer> cleanedAndSortedPcs = pcSet.stream()
                                                  .map(PitchSetUtils::normalizePc)
                                                  .distinct()
                                                  .sorted()
                                                  .collect(Collectors.toList());

        if (cleanedAndSortedPcs.isEmpty()) {
            return new RotationDetails(Collections.emptyList(), Collections.emptyList(), 0, Collections.emptyList());
        }

        if (cleanedAndSortedPcs.size() == 1) {
            List<Integer> singlePcRotation = Collections.singletonList(cleanedAndSortedPcs.get(0));
            return new RotationDetails(
                singlePcRotation,
                Collections.singletonList(0),
                0,
                Collections.emptyList()
            );
        }

        // 2. Genera tutte le rotazioni del set ordinato.
        List<List<Integer>> allRotations = PitchSetUtils.getRotations(cleanedAndSortedPcs);

        // 3. Prepara i candidati RotationDetails per il confronto.
        List<RotationDetails> candidates = new ArrayList<>();
        for (List<Integer> rot : allRotations) {
            
        	int root = rot.get(0);
            
        	List<Integer> distances = rot.stream()
						            		.map(pc -> PitchSetUtils.normalizePc(pc - root))
						            		.collect(Collectors.toList());
            
        	int span = PitchSetUtils.getSpan(distances);
            
        	List<Integer> intervals = PitchSetUtils.getSuccessiveIntervals(distances);
            candidates.add(new RotationDetails(rot, distances, span, intervals));
        }

        // 4. Trova la migliore RotationDetails usando il criterio standard della Normal Order.
        // Criteri di ordinamento:
        // a) Span minimo (l'intervallo tra il primo e l'ultimo elemento della forma 0-based).
        // b) Se span uguale, confronta lessicograficamente le sequenze degli intervalli successivi.
        // c) Se anche gli intervalli sono uguali (set simmetrici), scegli la rotazione che inizia con la pitch class più bassa.
        RotationDetails bestRotation = Collections.min(candidates, new Comparator<RotationDetails>() {
            @Override
            public int compare(RotationDetails objA, RotationDetails objB) {
                // Criterio 1: Span minimo
                int spanComparison = Integer.compare(objA.getSpan(), objB.getSpan());
                if (spanComparison != 0) {
                    return spanComparison;
                }

                // Criterio 2: Intervalli successivi lessicograficamente (più "left-packed")
                List<Integer> intervalsA = objA.getIntervals();
                List<Integer> intervalsB = objB.getIntervals();

                int len = Math.min(intervalsA.size(), intervalsB.size());
                for (int i = 0; i < len; i++) {
                    int intervalComparison = Integer.compare(intervalsA.get(i), intervalsB.get(i));
                    if (intervalComparison != 0) {
                        return intervalComparison;
                    }
                }
                // Criterio 3: Se span e intervalli sono identici, scegli la rotazione che inizia con la PC più bassa.
                // Questo è un tie-breaker per i set simmetrici con multiple Normal Orders equivalenti.
                return Integer.compare(objA.getRotation().get(0), objB.getRotation().get(0));
            }
        });

        return bestRotation;
    }

    /**
     * Calcola la Prime Form di un set di pitch classes.
     * La Prime Form è la rappresentazione più compatta (left-packed) di un set,
     * considerando sia il set originale che la sua inversione.
     * Questo metodo sfrutta l'algoritmo della Normal Order per efficienza.
     *
     * @param pcSet L'insieme di pitch classes di input (non necessariamente ordinato o pulito).
     * @return Una lista di interi che rappresenta la Prime Form.
     */
    public static List<Integer> getPrimeForm(List<Integer> pcSet) {
        // 1. Pulisci e ordina l'input per lavorare su un set canonico
        List<Integer> cleanedPcSet = pcSet.stream()
                                          .map(PitchSetUtils::normalizePc)
                                          .distinct()
                                          .sorted()
                                          .collect(Collectors.toList());

        if (cleanedPcSet.isEmpty()) {
            return Collections.emptyList();
        }
        if (cleanedPcSet.size() == 1) {
            return Collections.singletonList(0); // Prime form di un singolo elemento è [0]
        }

        // 2. Calcola la Normal Order del set originale
        RotationDetails originalNormObj = getNormalOrderObject(cleanedPcSet);
        List<Integer> noDistances = originalNormObj.getDistances(); // La NO trasposta a 0

        // Se il set ha meno di 2 elementi (già gestito sopra, ma per sicurezza negli indici)
        if (noDistances.size() < 2) {
            return noDistances;
        }

        // 3. Calcola gli intervalli tra i primi due elementi e gli ultimi due della Normal Order (0-based)
        int firstInterval = noDistances.get(1) - noDistances.get(0);
        int lastInterval = noDistances.get(noDistances.size() - 1) - noDistances.get(noDistances.size() - 2);

        // 4. Confronta gli intervalli per decidere se invertire
        // Se il primo intervallo è minore o uguale all'ultimo, la NO del set originale è la Prime Form.
        // Altrimenti, si calcola la NO dell'inversione.
        if (firstInterval <= lastInterval) {
            return noDistances;
        } else {
            // Per ottenere la Prime Form invertendo la Normal Order:
            // Si inverte il set [0, pc2, ..., pcN] attorno all'asse che fa diventare 0 l'ultimo elemento.
            // Questo è equivalente a (ultimo_elemento - pc_i) mod 12.
            int lastElementOfNO = noDistances.get(noDistances.size() - 1);
            List<Integer> invertedPrimeForm = noDistances.stream()
                                                         .map(pc -> PitchSetUtils.normalizePc(lastElementOfNO - pc))
                                                         .distinct()
                                                         .sorted()
                                                         .collect(Collectors.toList());
            return invertedPrimeForm;
        }
    }
}