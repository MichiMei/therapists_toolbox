package Host.Library.Controller;

import Library.UnimplementedException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * This class manages the data storage
 *
 * Storage is the "TherapistsToolbox" folder, containing a Index-file, a Sheet-storage-folder and a Picture-storage-folder
 * Additionally it contains a tmp folder for backups.
 */
public class Storage {

    private static final String BASE_DIR = "TherapistsToolbox";
    private static final String SHEET_DIR = "Sheets";
    private static final String PICTURE_DIR = "Pictures";
    private static final String TMP_DIR = "TMP";
    private static final String INDEX_FILE = "index.tti";
//    private static final String SHEET_EXT = ".tts";

    private final Path index;
    private final Path sheets;
    private final Path pictures;
    private final Path tmp;

    private final SheetTree root;

    /**
     * Checks the folder at the given path, if all necessary files and folders are available
     * Creates a Storage-manager (with imported Index-File)
     *
     * @param location path to the folder containing the "TherapistsToolbox" (Main) folder
     * @throws FaultyStorageStructureException thrown iff the Storage structure is damaged
     * @throws CorruptedStorageException thrown iff the Storage content is damaged
     *
     */
    public Storage (Path location) throws FaultyStorageStructureException, CorruptedStorageException, IOException {
        location = checkStorageStructure(location);
        this.index = location.resolve(INDEX_FILE);
        this.sheets = location.resolve(SHEET_DIR);
        this.pictures = location.resolve(PICTURE_DIR);
        this.tmp = location.resolve(TMP_DIR);

        this.root = new SheetTree(new BufferedReader(Files.newBufferedReader(this.index)));
    }

    /**
     * Checks the given Path for structure-damages
     * iff correct, performs also a full-check
     *
     * @param userPath Path to the storage system
     * @return Path of the base-dir iff correct
     * @throws FaultyStorageStructureException thrown iff the Storage structure is damaged
     * @throws CorruptedStorageException thrown iff the Storage content is damaged
     */
    public static Path checkStorageSystem(Path userPath) throws FaultyStorageStructureException, CorruptedStorageException, IOException {
        // check structure
        Path baseDir = checkStorageStructure(userPath);

        // check content
        Storage storage = new Storage(baseDir);
        storage.checkStorageContent();

        return baseDir;
    }

    /**
     * Checks the given Path for structure-damages
     *
     * @param userPath Path to the storage system
     * @return Path of the base-dir iff correct
     * @throws FaultyStorageStructureException thrown iff the storage structure is damaged
     */
    private static Path checkStorageStructure(Path userPath) throws FaultyStorageStructureException {
        Logger logger = Logger.getLogger(Storage.class.getName());
        logger.info("checking storage system '" + userPath.toString() + "'");
        Path baseDir;

        Path tmp = userPath.resolve(BASE_DIR);
        if (Files.isDirectory(tmp)) { // check if folder contains base-dir
            baseDir = tmp;
        } else if (Files.isDirectory(userPath) && userPath.getFileName().toString().equals(BASE_DIR)) { // check if parent-folder contains base-dir
            baseDir = userPath;
        } else { // base-dir not found
            logger.warning("base-dir not found");
            throw new FaultyStorageStructureException("base-dir missing (" + userPath.toString() + ")");
        }
        logger.info("base-dir found at '" + baseDir.toString() + "'");

        // check existence of index file
        Path indexFile = baseDir.resolve(INDEX_FILE);
        if (!Files.isRegularFile(indexFile)) {
            logger.warning("index-file missing");
            throw new FaultyStorageStructureException("index-file missing");
        }

        // check existence of sub-files
        Path sheetDir = baseDir.resolve(SHEET_DIR);
        if (!Files.isDirectory(sheetDir)) {
            logger.warning("sheet-dir missing");
            throw new FaultyStorageStructureException("sheet-dir missing");
        }
        Path pictureDir = baseDir.resolve(PICTURE_DIR);
        if (!Files.isDirectory(pictureDir)) {
            logger.warning("picture-dir missing");
            throw new FaultyStorageStructureException("picture-dir missing");
        }
        Path tmpDir = baseDir.resolve(TMP_DIR);
        if (!Files.isDirectory(tmpDir)) {
            logger.warning("tmp-dir missing");
            throw new FaultyStorageStructureException("tmp-dir missing");
        }

        logger.info("structure intact");
        return baseDir;
    }

    /**
     * Checks for Errors in the Storage
     * (checks all files and Pictures)
     *
     * @throws CorruptedStorageException thrown iff the Storage content is damaged
     */
    private void checkStorageContent() throws CorruptedStorageException {
        // TODO
        // throw new UnimplementedException("src/Host/Storage.java: checkStorageContent(...) is unimplemented");
    }

    /**
     * Moved the entire Storage to a new location
     *
     * @param newLocation path for the new storage location
     * @return true, iff moving was successful; false, iff something went wrong (all modifications reversed)
     */
    public boolean moveStorage(Path newLocation) {
        // TODO
        throw new UnimplementedException("src/Host/Storage.java: moveStorage(...) is unimplemented");
    }

    /**
     * Creates a new Storage at the given Location
     * Creates all necessary files and folders
     *
     * @param location path to the location of the new storage
     * @return Path of the base dir iff creation successful; null iff base-dir already existed
     * @throws IOException thrown by file or directory creation
     */
    public static Path initStorage(Path location) throws IOException {
        Logger logger = Logger.getLogger(Storage.class.getName());
        logger.info("creating new storage at " + location.toString());
        // create folder (TherapistsToolbox)
        Path baseDir = location.resolve(BASE_DIR);
        try {
            Files.createDirectories(baseDir);
            logger.info("base-dir created");
        } catch (FileAlreadyExistsException e) {
            logger.warning("'" + baseDir + "' already existed at this location");
            logger.warning(e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }

        // create index-file
        Path indexFile = baseDir.resolve(INDEX_FILE);
        Files.createFile(indexFile);
        logger.info("index-file created");
        BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(indexFile));
        writer.append("d").append("\0").append("TherapistsToolbox").append("\0").append("0").append("\n");
        writer.flush();

        // create sub-folders (Sheets, Pictures, tmp)
        Path sheetsDir = baseDir.resolve(SHEET_DIR);
        Path picturesDir = baseDir.resolve(PICTURE_DIR);
        Path tmpDir = baseDir.resolve(TMP_DIR);
        Files.createDirectories(sheetsDir);
        logger.info("sheets-dir created");
        Files.createDirectories(picturesDir);
        logger.info("pictures-dir created");
        Files.createDirectories(tmpDir);
        logger.info("tmp-dir created");

        logger.info("storage successfully created");
        return baseDir;
    }

    /**
     * Returns the default path for the storage (home)
     * @return path to the default storage location
     */
    public static Path defaultPath() {
        try {
            String tmp = System.getProperty("user.home");
            return Paths.get(tmp);
        } catch (Exception e) {
            e.printStackTrace();
            Logger logger = Logger.getLogger(Storage.class.getName());
            logger.warning("Could not get system-property 'user.home'");
            return null;
        }

    }

    public DefaultMutableTreeNode getJTreeRoot() {
        return root.getJTreeRoot();
    }

    public Info getInfo(TreePath path) throws FaultyStorageStructureException {
        return root.getInfo(path);
    }

    /**
     * Is thrown, iff a file or folder of the Storage-system (or FileSystem) is missing
     */
    public static class FaultyStorageStructureException extends Exception {
        public FaultyStorageStructureException(String msg) {
            super(msg);
        }
    }

    /**
     * Is thrown, iff a data file or picture is missing, or a file is corrupted
     */
    public static class CorruptedStorageException extends Exception {
        public CorruptedStorageException(String msg) {
            super(msg);
        }
    }

    public static class Info {
        private final boolean directory;
        private final String name;
        private final String path;
        private int minVersion;
        private int pages;
        private String description;
        private boolean randomized;

        public Info(String name, String path, int minVersion, int pages, String description, boolean randomized) {
            this.directory = false;
            this.name = name;
            this.path = path;
            this.minVersion = minVersion;
            this.pages = pages;
            this.description = description;
            this.randomized = randomized;
        }

        public Info(String name, String path) {
            this.directory = true;
            this.name = name;
            this.path = path;
        }

        public boolean isDirectory() {
            return directory;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public int getMinVersion() {
            return minVersion;
        }

        public int getPages() {
            return pages;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRandomized() {
            return randomized;
        }
    }

    private static class SheetTree {
        private abstract static class Node {
            protected String name;
            protected char type;
            protected DefaultMutableTreeNode jTreeNode;

            public abstract void save(PrintWriter writer) throws IOException;
            protected DefaultMutableTreeNode getJTreeNode() {
                return jTreeNode;
            }
            public abstract Info getInfo(TreePath path, int index) throws FaultyStorageStructureException;
        }
        private class Folder extends Node {
            private final TreeMap<String, Node> children;

            public Folder(String name) {
                this.name = name;
                this.type = 'd';
                this.children = new TreeMap<>();
                this.jTreeNode = new DefaultMutableTreeNode(this.name, true);
            }

            public Folder(String name, BufferedReader reader, int numberOfChildren) throws CorruptedStorageException, IOException {
                this.name = name;
                this.type = 'd';
                children = new TreeMap<>();
                this.jTreeNode = new DefaultMutableTreeNode(this.name, true);
                this.restoreChildren(reader, numberOfChildren);
            }

            @Override
            public void save(PrintWriter writer) throws IOException {
                writer.println(this.toString());
                for (Map.Entry<String, Node> child : children.entrySet()) {
                    child.getValue().save(writer);
                }
            }

            @Override
            public Info getInfo(TreePath path, int index) throws FaultyStorageStructureException {
                assert(index > 0);
                assert(index < path.getPathCount());

                if (index+1 == path.getPathCount()) return this.extractInfo(path);
                Node child = children.get(path.getPathComponent(index+1).toString());
                if (child == null) throw new FaultyStorageStructureException("Requested info for missing node");
                return child.getInfo(path, index+1);
            }

            private Info extractInfo(TreePath treePath) {
                StringBuilder path = new StringBuilder(treePath.getPathComponent(0).toString());
                for (int i = 1; i < treePath.getPathCount(); i++) path.append("\\").append(treePath.getPathComponent(i).toString());
                return new Info(this.name, path.toString());
            }

            @Override
            public String toString() {
                return this.type +
                        '\0' + this.name +
                        '\0' + this.children.size();
            }

            private void restoreChildren(BufferedReader reader, int numberOfChildren) throws CorruptedStorageException, IOException {
                for (int i = 0; i < numberOfChildren; i++) {
                    String line = reader.readLine();
                    if (line == null) {
                        throw new CorruptedStorageException("Index file: line missing");
                    }
                    String[] data = line.split("\0", -1);
                    if (data[0].equals("f") && data.length == 7) {
                        // create file
                        Node child = new File(data[1], data[2], Integer.parseInt(data[3]), Integer.parseInt(data[4]), data[5], Integer.parseInt(data[6]) == 1);
                        children.put(data[1], child);
                        this.jTreeNode.add(child.getJTreeNode());
                    } else if (data[0].equals("d") && data.length == 3) {
                        // create folder
                        Node child = new Folder(data[1], reader, Integer.parseInt(data[2]));
                        children.put(data[1], child);
                        this.jTreeNode.add(child.getJTreeNode());
                    } else {
                        throw new CorruptedStorageException("Index file: bad line: '" + line + "'");
                    }
                }
            }
        }
        private static class File extends Node {
            private final String fileName;
            private final int minVersion;
            private final int pages;
            private final String description;
            private final boolean randomized;

            public File (String name, String fileName, int minVersion, int pages, String description, boolean randomized) {
                this.name = name;
                this.fileName = fileName;
                this.minVersion = minVersion;
                this.pages = pages;
                this.description = description;
                this.randomized = randomized;
                this.jTreeNode = new DefaultMutableTreeNode(this.name, false);
            }

            @Override
            public void save(PrintWriter writer) {
                writer.write(this.toString());
            }

            @Override
            public Info getInfo(TreePath path, int index) {
                assert(index+1 == 0);
                return this.extractInfo(path);
            }

            private Info extractInfo(TreePath treePath) {
                StringBuilder path = new StringBuilder(treePath.getPathComponent(0).toString());
                for (int i = 1; i < treePath.getPathCount(); i++) path.append("\\").append(treePath.getPathComponent(i).toString());
                return new Info(this.name, path.toString(), this.minVersion, this.pages, this.description, this.randomized);
            }

            @Override
            public String toString() {
                String res =  this.type +
                        '\0' + this.name +
                        '\0' + this.fileName +
                        '\0' + this.minVersion +
                        '\0' + this.pages +
                        '\0' + this.description;
                if (randomized) res += "\0" + '1';
                else            res += "\0" + '0';
                return res;
            }
        }

        private final TreeMap<String, Node> children;
        private final String name = "TherapistsToolbox";
        private final char type = 'd';
        private final DefaultMutableTreeNode jTreeRoot;

        public SheetTree(BufferedReader reader) throws IOException, CorruptedStorageException {
            children = new TreeMap<>();
            jTreeRoot = new DefaultMutableTreeNode(this.name, true);
            this.restore(reader);
        }

        public void save(PrintWriter writer) throws IOException {
            writer.println(this.type + '\0' + this.name + '\0' + this.children.size());
            for (Map.Entry<String, Node> child : this.children.entrySet()) {
                child.getValue().save(writer);
            }
        }

        public void restore(BufferedReader reader) throws IOException, CorruptedStorageException {
            String line = reader.readLine();
            if (line == null) {
                throw new CorruptedStorageException("Index file empty");
            }
            String[] data = line.split("\0", -1);
            if (data.length != 3 || !data[0].equals("" + this.type) || !data[1].equals("" + this.name)) {
                System.out.println(data.length);
                System.out.println(data.length);
                throw new CorruptedStorageException("Index file: root wrong");
            }
            int numberOfChildren = Integer.parseInt(data[2]);
            for (int i = 0; i < numberOfChildren; i++) {
                this.jTreeRoot.add(restoreChild(reader).getJTreeNode());

            }
        }

        private Node restoreChild(BufferedReader reader) throws IOException, CorruptedStorageException {
            String line = reader.readLine();
            if (line == null) {
                throw new CorruptedStorageException("Index file: line missing");
            }
            String[] data = line.split("\0", -1);
            if (data[0].equals("f") && data.length == 7) {
                // create file
                Node child = new File(data[1], data[2], Integer.parseInt(data[3]), Integer.parseInt(data[4]), data[5], Integer.parseInt(data[6]) == 1);
                children.put(data[1], child);
                return child;
            } else if (data[0].equals("d") && data.length == 3) {
                // create folder
                Node child = new Folder(data[1], reader, Integer.parseInt(data[2]));
                children.put(data[1], child);
                return child;
            } else {
                throw new CorruptedStorageException("Index file: bad line: '" + line + "'");
            }
        }

        public DefaultMutableTreeNode getJTreeRoot() {
            return this.jTreeRoot;
        }

        public Info getInfo(TreePath path) throws FaultyStorageStructureException {
            if (!path.getPathComponent(0).toString().equals(this.name)) throw new FaultyStorageStructureException("Requested info for missing node");
            if (path.getPathCount() == 1) return extractInfo(path);
            Node child = children.get(path.getPathComponent(1).toString());
            if (child == null) throw new FaultyStorageStructureException("Requested info for missing node");
            return child.getInfo(path, 1);
        }

        private Info extractInfo(TreePath treePath) {
            StringBuilder path = new StringBuilder(treePath.getPathComponent(0).toString());
            for (int i = 1; i < treePath.getPathCount(); i++) path.append("\\").append(treePath.getPathComponent(i).toString());
            return new Info(this.name, path.toString());
        }
    }




}
