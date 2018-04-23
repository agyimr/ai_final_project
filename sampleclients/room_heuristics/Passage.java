package sampleclients.room_heuristics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Passage {
    Map map;
    Section[][][] section_map;
    java.util.Map<String, ArrayList<Section>> passages = new HashMap<>();
    java.util.Map<String, Section> sections = new HashMap<>();
    int width;
    int height;

    Passage(Map map, ArrayList<Section> sections1, ArrayList<Section> sections2) {
        this.map = map;
        this.width = map.width;
        this.height = map.height;
        sections1.forEach(section -> this.sections.put(section.id, section));
        sections2.forEach(section -> this.sections.put(section.id, section));

        applySections(sections1, sections2);
        removeSectionsInSections();
        setUpPassages();
    }

    public Section getSection(String id) {
        return this.sections.get(id);
    }

    public ArrayList<Path> getAllNeighbours(Point p) {
        ArrayList<Path> all_neighbours = new ArrayList<>();
        for (int i = 0; i <= 1; ++i) {
            if (this.section_map[p.y][p.x][i] != null) {
                for (Section to : this.passages.get(this.section_map[p.y][p.x][i].id)) {
                    Path path = new Path(to, this.section_map[p.y][p.x][i]);
                    all_neighbours.add(path);
                }
            }
        }
        if (all_neighbours.size() == 0) return null;
        return all_neighbours;
    }

    public int getDistanceFrom(Point from, Point to) {
        int d = Integer.MAX_VALUE;
        for (int i = 0; i <= 1; ++i) {
            if (this.section_map[to.y][to.x][i] != null) {
                int d_tmp = this.section_map[to.y][to.x][i].getDistanceFromPoint(from);
                if (d_tmp < d) d = d_tmp;
            }
        }
        if (d == Integer.MAX_VALUE) throw new Error("The goal is in no section (Probably in a wall)");
        return d;
    }

    private void applySections(ArrayList<Section> sections1, ArrayList<Section> sections2) {
        this.section_map = new Section[this.height][this.width][2];
        for (Section s : sections1) {
            for (int row = s.p1.y; row <= s.p2.y; ++row) {
                for (int col = s.p1.x; col <= s.p2.x; ++col) {
                    this.section_map[row][col][0] = s;
                }
            }
        }
        for (Section s : sections2) {
            for (int row = s.p1.y; row <= s.p2.y; ++row) {
                for (int col = s.p1.x; col <= s.p2.x; ++col) {
                    this.section_map[row][col][1] = s;
                }
            }
        }
    }

    private void removeSectionsInSections() {
        for (int row = 0; row < this.height; ++row) {
            for (int col = 0; col < this.width; ++col) {
                for (int dim = 0; dim <= 1; ++dim) {
                    int other_dim = 1 - dim;
                    if (this.section_map[row][col][dim] != null && this.section_map[row][col][other_dim] != null) {
                        if (this.section_map[row][col][dim].contains(this.section_map[row][col][other_dim])) {
                            this.section_map[row][col][other_dim] = null;
                        }
                    }
                }
            }
        }
    }

    private void setUpPassages() {
        for (int row = 0; row < this.height; ++row) {
            for (int col = 0; col < this.width; ++col) {
                for (int dim = 0; dim <= 1; ++dim) {
                    if (this.section_map[row][col][dim] != null) {
                        Section current_section = this.section_map[row][col][dim];
                        Set<Section> new_neighbours = new HashSet<>();

                        // checking for all the possible passages (neighbours of the current cell)
                        for (int i = -1; i <= 1; ++i) {
                            for (int k = -1; k<= 1; ++k) {

                                if ((i == 0 || k == 0) && (row + i >= 0 && row + i < this.height && col + k >= 0 && col + k < this.width)) { // make sure not checking in the cross direction
                                    for (int l = 0; l <= 1; l++) {
                                        if (this.section_map[row + i][col + k][l] != current_section && this.section_map[row + i][col + k][l] != null) {
                                            new_neighbours.add(this.section_map[row + i][col + k][l]);
                                        }
                                    }
                                }
                            }
                        }

                        // Extending neighbours hashmap with the new neighbours...
                        ArrayList<Section> neighbours_so_far = this.passages.get(current_section.id);
                        if (neighbours_so_far != null) {
                            new_neighbours.forEach((neighbour) -> {
                                if (!neighbours_so_far.contains(neighbour)) {
                                    neighbours_so_far.add(neighbour);
                                }
                            });
                            this.passages.put(current_section.id, neighbours_so_far);
                        } else {
                            this.passages.put(current_section.id, new ArrayList<>(new_neighbours));
                        }
                    }
                }
            }
        }
    }

    void PrintMap() {
        for (int dim = 0; dim <= 1; ++dim) {
            for (int row = 0; row < this.height; ++row) {
                for (int col = 0; col < this.width; ++col) {
                    if (this.map.character_map[row][col] == '+') {
                        System.out.print('+');
                    } else {
                        if (this.section_map[row][col][dim] != null) {
                            System.out.print(this.section_map[row][col][dim].id.charAt(0));
                        } else {
                            System.out.print(' ');
                        }
                    }
                }
                System.out.print('\n');
            }
            System.out.print('\n');
        }
    }
}
