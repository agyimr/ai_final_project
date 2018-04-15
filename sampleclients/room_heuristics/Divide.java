import java.awt.*;
import java.util.ArrayList;

public class Divide {
    static ArrayList<Section> DivideMap(Map map, boolean horizontally) {
        Point beginning = null;
        Point ending = null;
        ArrayList<Section> sections = new ArrayList<Section>();
        if (horizontally) {
            for (int y = 0; y < map.height; ++y) {
                for (int x = 0; x < map.width; ++x) {
                    if (map.isEmpty(x, y) && beginning != null) {
                        ending = new Point(x, y);
                    } else if (map.isEmpty(x, y) && beginning == null) {
                        beginning = new Point(x, y);
                        ending = new Point(x, y);
                    } else if (map.isWall(x, y) && beginning != null ) {
                        sections = MergeSections(sections, new Section(beginning, ending), horizontally);
                        beginning = null;
                        ending = null;
                    }
                }
                beginning = null;
                ending = null;
            }
        } else {
            for (int x = 0; x < map.width; ++x) {
                for (int y = 0; y < map.height; ++y) {
                    if (map.isEmpty(x, y) && beginning != null) {
                        ending = new Point(x, y);
                    } else if (map.isEmpty(x, y) && beginning == null) {
                        beginning = new Point(x, y);
                        ending = new Point(x, y);
                    } else if (map.isWall(x, y) && beginning != null ) {
                        sections = MergeSections(sections, new Section(beginning, ending), horizontally);
                        beginning = null;
                        ending = null;
                    }
                }
                beginning = null;
                ending = null;
            }
        }

        return sections;
    }

    static ArrayList<Section> MergeSections(ArrayList<Section> sections, Section newSection, boolean horizontally) {
        ArrayList<Section> newSections = sections;
        boolean merged = false;
        for (Section s : newSections) {
            if (horizontally) {
                if (s.p1.x == newSection.p1.x && s.p2.x == newSection.p2.x && s.p2.y + 1 == newSection.p2.y) {
                    s.p2 = newSection.p2;
                    merged = true;
                }
            } else {
                if (s.p1.y == newSection.p1.y && s.p2.y == newSection.p2.y && s.p2.x + 1 == newSection.p2.x) {
                    s.p2 = newSection.p2;
                    merged = true;
                }
            }

        }

        if (!merged) newSections.add(newSection);
        return newSections;
    }


}
