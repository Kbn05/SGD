package com.kbnprojects;

import com.kbnprojects.client.Client;
import com.kbnprojects.client.SocketProcess;
import com.kbnprojects.sgd.repository.FileManager;
import com.kbnprojects.socket.JavaClientSocket;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    private static BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();
    private static Path path = Paths.get("C:\\Users\\kevin\\Pictures\\bup");
    private static int notificationCount = 0;

    public static void main(String[] args) {

        JavaClientSocket clientSocket = new JavaClientSocket(5055, "localhost");
        Socket socket = clientSocket.get();
        SocketProcess client = new Client(socket);

        if (!client.connect()){
            System.out.println("Connection failed");
            return;
        }

        Thread socketThread = new Thread(() ->broadcastNotify(client));
        socketThread.start();

        JFrame frame = new JFrame("SGD UPB");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        JTextField tfSearch = new JTextField(10);
        JButton send = new JButton("Search");

        ImageIcon imgIconNotif = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/bell.png")));
        Image imgNotif = imgIconNotif.getImage();
        Image scaledImgNotif = imgNotif.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        Icon notifIcon = new ImageIcon(scaledImgNotif);
        JButton btnNotifications = new JButton();
        btnNotifications.setIcon(notifIcon);
        btnNotifications.setText(null);
        btnNotifications.setContentAreaFilled(false);

        ImageIcon imgIconUpload = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/upload.png")));
        Image imgUpload = imgIconUpload.getImage();
        Image scaledImgUpload = imgUpload.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        Icon uploadIcon = new ImageIcon(scaledImgUpload);
        JButton btnFileUpload = new JButton();
        btnFileUpload.setIcon(uploadIcon);
        btnFileUpload.setText(null);
        btnFileUpload.setContentAreaFilled(false);

//        btnNotifications.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 20));
//        btnFileVersions.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 20));

        searchPanel.add(tfSearch);
        searchPanel.add(send);
        searchPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        searchPanel.add(btnNotifications);
        searchPanel.add(btnFileUpload);

        JPanel folderPanel = new JPanel();
        folderPanel.setPreferredSize(new Dimension(300, frame.getHeight()));

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setPreferredSize(new Dimension(500, frame.getHeight()));

//        JList<File> dirList = directoryList(filePanel);
//        JScrollPane scrollPane = new JScrollPane(dirList);
//        scrollPane.setPreferredSize(new Dimension(175, frame.getHeight()));
//        folderPanel.add(scrollPane);

        JTree folderTree = directoryTree(filePanel);
        JScrollPane treeScrollPane = new JScrollPane(folderTree);
        folderPanel.add(treeScrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(filePanel, BorderLayout.CENTER);
        frame.getContentPane().add(BorderLayout.NORTH, searchPanel);
        frame.getContentPane().add(BorderLayout.WEST, folderPanel);
        frame.setVisible(true);

//        Thread notificationThread = new Thread(()->{
//            while (true){
//                try {
//                    String peek = eventQueue.take();
//                    System.out.println(peek);
//                    if (peek != null){
//                        notificationCount++;
//                        btnNotifications.setText(String.valueOf(notificationCount));
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//        notificationThread.start();

        btnNotifications.addActionListener(e -> {
            StringBuilder notifications = new StringBuilder();

            eventQueue.forEach(notif -> notifications.append(notif).append("\n"));

            if (!notifications.isEmpty()) {
                JOptionPane.showMessageDialog(null, notifications.toString(), "Notificaciones", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No hay nuevas notificaciones.", "Notificaciones", JOptionPane.INFORMATION_MESSAGE);
            }
            notificationCount = 0;
            btnNotifications.setText(null);
        });

        btnFileUpload.addActionListener(e->{
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null); // Abre el diálogo para seleccionar archivo

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Archivo seleccionado: " + selectedFile.getPath());
                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", 5056);
                    FileManager fileManager = (FileManager) registry.lookup("fileManager");
                    byte[] fileContent = Files.readAllBytes(selectedFile.toPath());

                    fileManager.uploadFile(fileContent, String.valueOf(path));
                    System.out.println("File uploaded");
                } catch (NotBoundException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        send.addActionListener(e->{
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 5056);
                FileManager fileManager = (FileManager) registry.lookup("fileManager");
                String[] fileObject = fileManager.listVersions("hi");
                System.out.println(Arrays.toString(fileObject));
            } catch (RemoteException | NotBoundException ex) {
                ex.printStackTrace();
            }
        });

    }

    private static void filterFiles(JPanel filePanel, File directory, String name, String type, String date) {
        filePanel.removeAll();

        DefaultListModel<File> model = new DefaultListModel<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory.toPath())) {
            for (Path entry : stream) {
                File file = entry.toFile();

                if (!name.isEmpty() && !file.getName().toLowerCase().contains(name.toLowerCase())) {
                    continue;
                }

                if (!type.isEmpty() && !file.getName().toLowerCase().endsWith(type.toLowerCase())) {
                    continue;
                }

                if (!date.isEmpty()) {
                    long lastModified = file.lastModified();
                    String fileDate = dateFormat.format(new Date(lastModified));

                    if (!fileDate.equals(date)) {
                        continue;
                    }
                }

                model.addElement(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JList<File> fileList = new JList<>(model);
        fileList.setCellRenderer(new FileRenderer());
        JScrollPane scrollPane = new JScrollPane(fileList);

        filePanel.add(scrollPane, BorderLayout.CENTER);
        filePanel.revalidate();
        filePanel.repaint();
    }

//    private static JTree directoryTree(JPanel filePanel) {
//        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new File(path.toString()));
//        createNodes(root, path.toFile());
//
//        JTree tree = new JTree(root);
//        tree.setRootVisible(true);
//        tree.setShowsRootHandles(true);
//
//
//        tree.addTreeSelectionListener(e -> {
//            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
//            if (selectedNode == null) return;
//
//            File selectedFile = (File) selectedNode.getUserObject();
//            if (selectedFile.isDirectory()) {
//                showDirectoryContent(filePanel, selectedFile);
//            }
//        });
//
//        return tree;
//    }

    private static JTree directoryTree(JPanel filePanel) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new File(path.toString()));
        createNodes(root, path.toFile());

        JTree tree = new JTree(root);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        tree.setCellRenderer(new FolderTreeCellRenderer());

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null) return;

            File selectedFile = (File) selectedNode.getUserObject();
            if (selectedFile.isDirectory()) {
                showDirectoryContent(filePanel, selectedFile);
            }
        });

        return tree;
    }

    static class FolderTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            File file = (File) node.getUserObject();

            if (file != null && !node.isRoot()) {
                label.setText(file.getName());
            }

            return label;
        }
    }

    private static void createNodes(DefaultMutableTreeNode node, File file) {
        File[] files = file.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(f);
                node.add(childNode);
                createNodes(childNode, f);
            }
        }
    }

//    private static JList<File> directoryList(JPanel filePanel){
//        DefaultListModel<File> model = new DefaultListModel<>();
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
//            for (Path entry : stream) {
//                if (Files.isDirectory(entry)){
//                    model.addElement(entry.toFile());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        JList<File> directoryList = new JList<>(model);
//        directoryList.setCellRenderer(new FolderRenderer());
//
//        directoryList.addListSelectionListener(e -> {
//            if (!e.getValueIsAdjusting()) {
//                File selectedDirectory = directoryList.getSelectedValue();
//                if (selectedDirectory != null && selectedDirectory.isDirectory()) {
//                    showDirectoryContent(filePanel, selectedDirectory);
//                }
//            }
//        });
//
//        return directoryList;
//    }

    private static void showDirectoryContent(JPanel filePanel, File directory) {
        filePanel.removeAll();

        DefaultListModel<File> model = new DefaultListModel<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory.toPath())) {
            for (Path entry : stream) {
                if (!entry.toFile().isDirectory()){
                    model.addElement(entry.toFile());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JList<File> fileList = new JList<>(model);
        fileList.setCellRenderer(new FileRenderer());
        JScrollPane scrollPane = new JScrollPane(fileList);

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem downloadItem = new JMenuItem("Descargar");
        JMenuItem renameItem = new JMenuItem("Renombrar");
        JMenuItem deleteItem = new JMenuItem("Eliminar");
        JMenuItem versionItem = new JMenuItem("Ver versiones");
        JMenuItem propertiesItem = new JMenuItem("Propiedades");

        popupMenu.add(downloadItem);
        popupMenu.add(renameItem);
        popupMenu.add(deleteItem);
        popupMenu.add(versionItem);
        popupMenu.add(propertiesItem);

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = fileList.locationToIndex(e.getPoint());
                    fileList.setSelectedIndex(index);
                    File selectedFile = fileList.getSelectedValue();

                    popupMenu.show(fileList, e.getX(), e.getY());

                    downloadItem.addActionListener(event -> downloadFile(selectedFile));
                    renameItem.addActionListener(event -> renameFile(selectedFile, fileList, model, index));
                    deleteItem.addActionListener(event -> deleteFile(selectedFile));
                    versionItem.addActionListener(event -> versionFile(selectedFile));
                    propertiesItem.addActionListener(event -> showFileProperties(selectedFile));
                }
            }
        });

        filePanel.add(scrollPane, BorderLayout.CENTER);
        filePanel.revalidate();
        filePanel.repaint();
    }

    private static void versionFile(File selectedFile) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5056);
            FileManager fileManager = (FileManager) registry.lookup("fileManager");
            System.out.println(selectedFile.getPath());
            fileManager.listVersions(selectedFile.getPath());
        } catch (RemoteException | NotBoundException ex) {
            ex.printStackTrace();
        }
    }

    private static void deleteFile(File selectedFile) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5056);
            FileManager fileManager = (FileManager) registry.lookup("fileManager");
            System.out.println(selectedFile.getPath());
            fileManager.deleteFile(selectedFile.getPath());
            System.out.println("Done");
        } catch (RemoteException | NotBoundException ex) {
            ex.printStackTrace();
        }
    }

    private static void downloadFile(File file) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5056);
            FileManager fileManager = (FileManager) registry.lookup("fileManager");
            byte[] fileObject = fileManager.downloadFile(file.getPath());
            try (FileOutputStream fos = new FileOutputStream("C:\\Users\\kevin\\Downloads\\" + file.getName())) {
                fos.write(fileObject);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (RemoteException | NotBoundException ex) {
            ex.printStackTrace();
        }
    }

    private static void renameFile(File file, JList<File> fileList, DefaultListModel<File> model, int index) {
        String newName = JOptionPane.showInputDialog(null, "Nuevo nombre:", file.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            File renamedFile = new File(file.getParent(), newName);
            if (file.renameTo(renamedFile)) {
                model.set(index, renamedFile);
                fileList.repaint();
                JOptionPane.showMessageDialog(null, "Archivo renombrado a: " + renamedFile.getName());
            } else {
                JOptionPane.showMessageDialog(null, "Error al renombrar el archivo.");
            }
        }
    }

//    private static void showFileProperties(File file) {
//        long fileSize = file.length();
//        String message = "Nombre: " + file.getName() + "\n" +
//                "Ruta: " + file.getAbsolutePath() + "\n" +
//                "Tamaño: " + fileSize + " bytes";
//        JOptionPane.showMessageDialog(null, message, "Propiedades del archivo", JOptionPane.INFORMATION_MESSAGE);
//    }

    private static void showFileProperties(File file) {
        long fileSize = file.length();
        String permissions = getFilePermissions(file);
        Date date = new Date(file.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(date);

        String message = "Nombre: " + file.getName() + "\n" +
                "Ruta: " + file.getAbsolutePath() + "\n" +
                "Tamaño: " + fileSize + " bytes" + "\n" +
                "Última modificación: " + formattedDate + "\n" +
                "Permisos: " + permissions;
        JOptionPane.showMessageDialog(null, message, "Propiedades del archivo", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String getFilePermissions(File file) {
        if (Files.isReadable(file.toPath())) {
            try {
                Set<PosixFilePermission> posixPermissions = Files.getPosixFilePermissions(file.toPath());
                return PosixFilePermissions.toString(posixPermissions);
            } catch (UnsupportedOperationException | IOException e) {
                return (file.canRead() ? "r" : "-") +
                        (file.canWrite() ? "w" : "-") +
                        (file.canExecute() ? "x" : "-");
            }
        }
        return "No se pueden determinar los permisos.";
    }

//    static class FolderRenderer extends DefaultListCellRenderer {
//        private final Icon folderIcon;
//
//        public FolderRenderer() {
//            ImageIcon imgIcon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/folder.png")));
//            Image img = imgIcon.getImage();
//            Image scaledImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
//            folderIcon = new ImageIcon(scaledImg);
//        }
//
//        @Override
//        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
//                                                      boolean isSelected, boolean cellHasFocus) {
//            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//
//            File file = (File) value;
//            label.setText(file.getName());
//            label.setIcon(folderIcon);
//            return label;
//        }
//    }

    static class FileRenderer extends DefaultListCellRenderer {
        private final Icon fileIcon;

        public FileRenderer() {
            ImageIcon imgIcon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/file.png")));
            Image img = imgIcon.getImage();
            Image scaledImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            fileIcon = new ImageIcon(scaledImg);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            File file = (File) value;
            label.setText(file.getName());
            label.setIcon(fileIcon);
            return label;
        }
    }

    private static void broadcastNotify(SocketProcess client){
        while (true){
            List<Object> listen = client.listen();
            eventQueue.add((String) listen.get(1));
            System.out.println(listen);
        }
    }
}