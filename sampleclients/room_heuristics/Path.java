package sampleclients.room_heuristics;

public class Path {
    public Section to;
    public Section through;

    Path(Section to, Section through) {
        this.to = to;
        this.through = through;
    }
}
