import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.sql.*;

public class Menu extends JFrame{
    public static void main(String[] args) {
        // buat object window
        Menu window = new Menu();

        // atur ukuran window
        window.setSize(400, 300);
        // letakkan window di tengah layar
        window.setLocationRelativeTo(null);
        // isi window
        window.setContentPane(window.mainPanel);
        // ubah warna background
        window.getContentPane().setBackground(Color.white);
        // tampilkan window
        window.setVisible(true);
        // agar program ikut berhenti saat window diclose
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;

    private Database database;

    private String RadioButtonDipiih = ""; // untuk menentukan RadioButton

    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;
    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;
    private JTextField nilaiField;
    private JLabel nilaiLabel;
    private JRadioButton nimRadioButton;
    private JRadioButton namaRadioButton;
    private JLabel filterLabel;
    private JTextField searchField;
    private JTextField searchnamaField;
    private JButton filterButton;

    // constructor
    public Menu() {
        // inisialisasi listMahasiswa
        listMahasiswa = new ArrayList<>();

        // objek databse
        database = new Database();

        /* membuat button agar bisa hanya dipilih satu */
        // Buat objek ButtonGroup
        ButtonGroup buttonGroup = new ButtonGroup();
        // Tambahkan RadioButton ke ButtonGroup
        buttonGroup.add(nimRadioButton);
        buttonGroup.add(namaRadioButton);

        // isi listMahasiswa
        populateList();

        // isi tabel mahasiswa
        mahasiswaTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD,20f));

        // atur isi combo box
        String[] jenisKelaminData ={"Laki-Laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(jenisKelaminData));

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // menambah filter
        filterButton.setVisible(false);
        searchField.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedIndex == -1)
                {
                    insertData();
                }
                else
                {
                    updateData();
                }
            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedIndex >=0)
                {
                    deleteData();
                }
            }
        });

        // ketika Nim Filter di klik
        nimRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(nimRadioButton.isSelected()) // jika memang memang Nim radio button dipilih
                {
                    searchField.setVisible(true); // untuk pengisian input akan ditampilkan
                    filterButton.setVisible(true); // tampilan button filternya juga ditampilkan
                    RadioButtonDipiih = "NIM"; // yang dipilih sekarang adalah NIM
                }
                else // jika bukan nim button yang dipilih
                {
                    searchField.setVisible(false); // input akan disembunyikan
                }
                mainPanel.revalidate(); // 2 fungsi ini aku tidak ngerti tapi agar bisa menampilkan fieldnya (dari chatgpt awkoawkaok)
                mainPanel.repaint();
            }
        });

        // ketika button nama filter di klik
        namaRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(namaRadioButton.isSelected()) // jika memang dipilih
                {
                    searchField.setVisible(true); // input field akan ditampilkan
                    filterButton.setVisible(true); // filter tombol akan ditampilkan juga
                    RadioButtonDipiih = "Nama"; // yang dpilih sekarang adalah Nama
                }
                else // jika tidak dipilih
                {
                    searchField.setVisible(false); // input bagian nama akan disembunyikan
                }
                mainPanel.revalidate(); // intinya agar bisa menampilkan input field radioButton dari kedua fungsi ini
                mainPanel.repaint();
            }
        });

        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String search = searchField.getText(); // input untuk mencari
                if(!search.isEmpty()) // jika memang terdapat data
                {
                    String sql = "";

                    if(RadioButtonDipiih.equals("NIM"))
                    {
                        sql = "SELECT * FROM Mahasiswa WHERE nim = '" + search + "'";
                    }
                    else if(RadioButtonDipiih.equals("Nama"))
                    {
                        sql = "SELECT * FROM Mahasiswa WHERE nama = '" + search + "'";
                    }
                    ResultSet rs = database.selectQuery(sql);
                    try
                    {
                        DefaultTableModel model = (DefaultTableModel) mahasiswaTable.getModel();
                        if(rs.next())
                        {
                            // mencari baris yang cocok dalam model
                            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                                if (model.getValueAt(i, 1).equals(rs.getString("nim")) && RadioButtonDipiih.equals("NIM") || model.getValueAt(i, 2).equals(rs.getString("nama")) && RadioButtonDipiih.equals("Nama")) {
                                    // jika baris yang cocok ditemukan, simpan data baris
                                    Object[] row = new Object[]{model.getValueAt(i, 0), model.getValueAt(i, 1), model.getValueAt(i, 2), model.getValueAt(i, 3), model.getValueAt(i, 4)};
                                    // hapus baris dari model
                                    model.removeRow(i);
                                    // tambahkan baris ke paling atas
                                    model.insertRow(0, row);
                                }
                            }
                        }
                        else
                        { // jika tidak ketemu
                            // akan menampilkan kalau data tersebut tidak ditemukan
                            JOptionPane.showMessageDialog(null, "Data tersebut tidak ditemukan!");
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                    buttonGroup.clearSelection(); // atur ulang RadioButton
                    searchField.setText(""); // kosongkan field pencarian
                    searchField.setVisible(false); // sembunyikan field pencarian
                    filterButton.setVisible(false); // sembunyikan tombol filter
                    RadioButtonDipiih = ""; // kosongkan button yang dipilih
                }
                else // jika data list kosong
                {
                    JOptionPane.showMessageDialog(null, "Data Kosong!");
                }
            }
        });

        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // saat salah satu baris tabel ditekan
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = mahasiswaTable.getSelectedRow();

                // simpan value textfield dan combo box
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex,1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex,2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex,3).toString();
                String selectedNilai  = mahasiswaTable.getModel().getValueAt(selectedIndex,4).toString();

                // ubah isi textfield dan combo box
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);
                nilaiField.setText(selectedNilai);

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");
                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] column = {"No", "Nim","Nama", "Jenis Kelamin", "Nilai"};

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel temp = new DefaultTableModel(null, column);
        try{
            ResultSet resultSet = database.selectQuery("Select * FROM mahasiswa");
            int i = 0;
            while(resultSet.next())
            {
                Object[] row = new Object[5];
                row[0] = i + 1;
                row[1] = resultSet.getString("nim");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("jenis_Kelamin");
                row[4] = resultSet.getString("Nilai");

                temp.addRow(row);
                i++;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return temp;
    }

    public void insertData()
    {
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String nilai = nilaiField.getText();
        int nimSama = 0;

        if(!nim.isEmpty() && !nama.isEmpty())
        {
            for(Mahasiswa m : listMahasiswa)
            {
                if(m.getNim().equals(nim))
                {
                    JOptionPane.showMessageDialog(null, "terdapat NIM yang sama!");
                    nimSama = 1;
                }
            }
            if(nimSama == 0)
            {
                listMahasiswa.add(new Mahasiswa(nim, nama, jenisKelamin, nilai));
                String sql = "INSERT INTO mahasiswa VALUE (null, '" + nim + "', '" + nama + "', '" + jenisKelamin + "', '" + nilai + "');";
                database.upToNow(sql);
                mahasiswaTable.setModel(setTable());
                clearForm();
                System.out.println("Insert Berhasil");
                JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "terdapat input yang kosong!");
        }
    }

    public void updateData()
    {
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String nilai = nilaiField.getText();
        if(!nim.isEmpty() && !nama.isEmpty())
        {
             int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin mengupdate data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
             if(confirm == JOptionPane.YES_OPTION)
             {
                 String nimSelected = listMahasiswa.get(selectedIndex).getNim();
                 listMahasiswa.get(selectedIndex).setNim(nim);
                 listMahasiswa.get(selectedIndex).setNama(nama);
                 listMahasiswa.get(selectedIndex).setJenisKelamin(jenisKelamin);
                 listMahasiswa.get(selectedIndex).setNilai(nilai);

                 String sql = "UPDATE mahasiswa SET nim = '" + nim + "', nama = '" + nama + "', jenis_kelamin = '" + jenisKelamin + "', nilai = '" + nilai + "' WHERE nim = '" + nimSelected + "';";
                 database.upToNow(sql);
                 mahasiswaTable.setModel(setTable());
                 clearForm();
                 System.out.println("Update Berhasil");
                 JOptionPane.showMessageDialog(null, "Data berhasil diubah!");
             }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "terdapat input yang kosong!");
        }
    }

    public void deleteData()
    {
        int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION)
        {
            String nim = listMahasiswa.get(selectedIndex).getNim();
            listMahasiswa.remove(selectedIndex);
            String sql = "DELETE FROM mahasiswa WHERE nim = '" + nim + "';";
            database.upToNow(sql);
            mahasiswaTable.setModel(setTable());
            clearForm();
            System.out.println("Delete Berhasil");
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus");
        }
    }



    public void clearForm() {
        // kosongkan semua texfield dan combo box
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");
        nilaiField.setText("");

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");
        // sembunyikan button delete
        deleteButton.setVisible(false);
        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;

        searchField.setText(""); // kosongkan field pencarian
        searchField.setVisible(false); // sembunyikan field pencarian
        filterButton.setVisible(false); // sembunyikan tombol filter
        RadioButtonDipiih = "";

    }

    private void populateList() {
        listMahasiswa.add(new Mahasiswa("2203999", "Amelia Zalfa Julianti", "Perempuan", "A"));
        listMahasiswa.add(new Mahasiswa("2202292", "Muhammad Iqbal Fadhilah", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2202346", "Muhammad Rifky Afandi", "Laki-laki", "B"));
        listMahasiswa.add(new Mahasiswa("2210239", "Muhammad Hanif Abdillah", "Laki-laki", " B+"));
        listMahasiswa.add(new Mahasiswa("2202046", "Nurainun", "Perempuan", "B+"));
        listMahasiswa.add(new Mahasiswa("2205101", "Kelvin Julian Putra", "Laki-laki", "A+"));
        listMahasiswa.add(new Mahasiswa("2200163", "Rifanny Lysara Annastasya", "Perempuan", "A"));
        listMahasiswa.add(new Mahasiswa("2202869", "Revana Faliha Salma", "Perempuan", "C+"));
        listMahasiswa.add(new Mahasiswa("2209489", "Rakha Dhifiargo Hariadi", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2203142", "Roshan Syalwan Nurilham", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2200311", "Raden Rahman Ismail", "Laki-laki", "B"));
        listMahasiswa.add(new Mahasiswa("2200978", "Ratu Syahirah Khairunnisa", "Perempuan", "C+"));
        listMahasiswa.add(new Mahasiswa("2204509", "Muhammad Fahreza Fauzan", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2205027", "Muhammad Rizki Revandi", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2203484", "Arya Aydin Margono", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2200481", "Marvel Ravindra Dioputra", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2209889", "Muhammad Fadlul Hafiizh", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2206697", "Rifa Sania", "Perempuan", "A"));
        listMahasiswa.add(new Mahasiswa("2207260", "Imam Chalish Rafidhul Haque", "Laki-laki", "A"));
        listMahasiswa.add(new Mahasiswa("2204343", "Meiva Labibah Putri", "Perempuan", "A"));
    }
}
