package Host.Library;

import Library.ContentClasses.Page;
import Library.UnimplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WorkSheet {

    private ArrayList<Page> pages;
    private boolean randomize;
    private int maxType;
    private String name;
    private String directory;
    private Iterator<Page> iter;
    private boolean changed;
    private String description;

    public WorkSheet (String name, String directory, boolean randomize) {
        this.pages = new ArrayList<>();
        this.randomize = randomize;
        this.maxType = 0;
        this.name = name;
        this.directory = directory;
        if (!randomize) {
            this.iter = pages.iterator();
        } else {
            List<Page> random = new ArrayList<>(pages);
            Collections.shuffle(random);
            this.iter = random.iterator();
        }
        this.changed = false;
    }

    public boolean isRandomize() {
        return randomize;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
        changed = true;
    }

    public int getMaxType() {
        return maxType;
    }

    public String getName() {
        return name;
    }

    public String getDirectory() {
        return directory;
    }

    public void changeDirectory(String directory) throws UnimplementedException {
        // TODO move work sheet
        throw new UnimplementedException("src/Host/Library/WorkSheet.java: changeDirectory(...) is unimplemented");
        // this.directory = directory;
    }

    public void changeName(String name) throws UnimplementedException {
        // TODO move work sheet
        throw new UnimplementedException("src/Host/Library/WorkSheet.java: changeName(...) is unimplemented");
        // this.name = name;
    }

    public void appendPage(Page page) {
        this.pages.add(page);
        changed = true;
    }

    public void addPage(Page page, int index) {
        this.pages.add(index, page);
        changed = true;
    }

    public void removePage(int index) {
        this.pages.remove(index);
        changed = true;
    }

    public void modifyPage(Page page, int index) {
        this.pages.set(index, page);
        changed = true;
    }

    public void save() throws UnimplementedException {
        if (!changed) return;
        // TODO save to drive
        throw new UnimplementedException("src/Host/Library/WorkSheet.java: save(...) is unimplemented");
    }
}
