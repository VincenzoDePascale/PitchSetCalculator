import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RotationDetails {
    private final List<Integer> rotation;   // La rotazione originale (es. [4, 7, 0])
    private final List<Integer> distances;  // La rotazione trasposta a 0 (es. [0, 3, 8])
    private final int span;                 // L'ampiezza di `distances` (es. 8)
    private final List<Integer> intervals;  // Gli intervalli successivi di `distances` (es. [3, 5])

    public RotationDetails(List<Integer> rotation, List<Integer> distances, int span, List<Integer> intervals) {
        // Creiamo copie immutabili delle liste per evitare modifiche esterne inattese
        this.rotation = Collections.unmodifiableList(rotation != null ? rotation : Collections.emptyList());
        this.distances = Collections.unmodifiableList(distances != null ? distances : Collections.emptyList());
        this.span = span;
        this.intervals = Collections.unmodifiableList(intervals != null ? intervals : Collections.emptyList());
    }

    // Metodi getter per accedere ai dati
    public List<Integer> getRotation() {
        return rotation;
    }

    public List<Integer> getDistances() {
        return distances;
    }

    public int getSpan() {
        return span;
    }

    public List<Integer> getIntervals() {
        return intervals;
    }

    @Override
    public String toString() {
        return "RotationDetails{" +
               "rotation=" + rotation +
               ", distances=" + distances +
               ", span=" + span +
               ", intervals=" + intervals +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RotationDetails that = (RotationDetails) o;
        return span == that.span &&
               Objects.equals(rotation, that.rotation) &&
               Objects.equals(distances, that.distances) &&
               Objects.equals(intervals, that.intervals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rotation, distances, span, intervals);
    }
}