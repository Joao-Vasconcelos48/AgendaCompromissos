package com.agenda.ui;

import com.agenda.dao.CompromissoDAO;
import com.agenda.dao.ContatoDAO;
import com.agenda.model.Compromisso;
import com.agenda.model.Contato;
import com.agenda.validation.EmailValidator;
import com.agenda.validation.PhoneValidator;
import com.agenda.util.CsvExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MainController {
    @FXML
    private ListView<Contato> listView;
    @FXML
    private TextField nomeField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telefoneField;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;

    // compromissos UI
    @FXML
    private ListView<Compromisso> compListView;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeField;
    @FXML
    private TextField localField;
    @FXML
    private CheckBox onlineCheck;
    @FXML
    private Button saveCompButton;
    @FXML
    private Button deleteCompButton;

    private final ContatoDAO dao = new ContatoDAO();
    private final CompromissoDAO compDao = new CompromissoDAO();
    private final ObservableList<Contato> contatos = FXCollections.observableArrayList();
    private final ObservableList<Compromisso> compromissos = FXCollections.observableArrayList();

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        listView.setItems(contatos);
        loadAll();

        compListView.setItems(compromissos);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                showContato(newV);
                loadCompromissosForContato(newV.getId());
            } else {
                clearForm();
                compromissos.clear();
            }
        });

        compListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) showCompromisso(newV);
            else clearCompForm();
        });
    }

    private void loadAll() {
        contatos.setAll(dao.findAll());
    }

    private void showContato(Contato c) {
        nomeField.setText(c.getNome());
        emailField.setText(c.getEmail());
        telefoneField.setText(c.getTelefone());
        deleteButton.setDisable(false);
    }

    private void clearForm() {
        nomeField.clear();
        emailField.clear();
        telefoneField.clear();
        deleteButton.setDisable(true);
    }

    @FXML
    void onSave() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "O nome é obrigatório").showAndWait();
            return;
        }

        String email = emailField.getText().trim();
        String telefone = telefoneField.getText().trim();

        // validar email (se preenchido) e telefone (se preenchido)
        if (!email.isEmpty() && !EmailValidator.isValid(email)) {
            new Alert(Alert.AlertType.WARNING, "Email inválido").showAndWait();
            return;
        }
        if (!telefone.isEmpty() && !PhoneValidator.isValid(telefone)) {
            new Alert(Alert.AlertType.WARNING, "Telefone inválido").showAndWait();
            return;
        }

        Contato selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Contato c = new Contato(nome, email, telefone);
            if (dao.insert(c)) {
                contatos.add(c);
                listView.getSelectionModel().select(c);
            } else {
                new Alert(Alert.AlertType.ERROR, "Erro ao inserir contato").showAndWait();
            }
        } else {
            selected.setNome(nome);
            selected.setEmail(email);
            selected.setTelefone(telefone);
            if (dao.update(selected)) {
                // refresh list
                listView.refresh();
            } else {
                new Alert(Alert.AlertType.ERROR, "Erro ao atualizar contato").showAndWait();
            }
        }
    }

    @FXML
    void onExportCsv() {
        Window window = listView.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar contatos para CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        chooser.setInitialFileName("contatos.csv");
        File file = chooser.showSaveDialog(window);
        if (file == null) return;

        try {
            // ensure latest data
            List<Contato> all = dao.findAll();
            CsvExporter.export(all, file);
            new Alert(Alert.AlertType.INFORMATION, "Exportado com sucesso: " + file.getAbsolutePath()).showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao exportar CSV: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onNew() {
        listView.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML
    void onDelete() {
        Contato selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confirma exclusão de " + selected.getNome() + "?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                if (dao.delete(selected.getId())) {
                    contatos.remove(selected);
                    clearForm();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Erro ao excluir").showAndWait();
                }
            }
        });
    }

    // ---------- compromissos logic ----------
    private void loadCompromissosForContato(Integer contatoId) {
        if (contatoId == null) return;
        compromissos.setAll(compDao.findByContatoId(contatoId));
    }

    private void showCompromisso(Compromisso c) {
        if (c == null) return;
        LocalDateTime dt = c.getDateTime();
        if (dt != null) {
            datePicker.setValue(dt.toLocalDate());
            timeField.setText(dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            datePicker.setValue(null);
            timeField.clear();
        }
        localField.setText(c.getLocal());
        onlineCheck.setSelected(c.isOnline());
        deleteCompButton.setDisable(false);
    }

    private void clearCompForm() {
        datePicker.setValue(null);
        timeField.clear();
        localField.clear();
        onlineCheck.setSelected(false);
        deleteCompButton.setDisable(true);
    }

    @FXML
    void onSaveComp() {
        Contato selectedContato = listView.getSelectionModel().getSelectedItem();
        if (selectedContato == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um contato antes de criar um compromisso").showAndWait();
            return;
        }

        LocalDate date = datePicker.getValue();
        String timeText = timeField.getText().trim();
        if (date == null || timeText.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Data e hora são obrigatórios").showAndWait();
            return;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeText);
        } catch (DateTimeParseException ex) {
            // try HH:mm
            try {
                time = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ex2) {
                new Alert(Alert.AlertType.WARNING, "Hora inválida. Use HH:mm").showAndWait();
                return;
            }
        }

        LocalDateTime dateTime = LocalDateTime.of(date, time);
        String local = localField.getText().trim();
        boolean online = onlineCheck.isSelected();

        Compromisso selected = compListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Compromisso c = new Compromisso(selectedContato.getId(), dateTime, local, online);
            if (compDao.insert(c)) {
                compromissos.add(c);
                compListView.getSelectionModel().select(c);
            } else {
                new Alert(Alert.AlertType.ERROR, "Erro ao inserir compromisso").showAndWait();
            }
        } else {
            selected.setContatoId(selectedContato.getId());
            selected.setDateTime(dateTime);
            selected.setLocal(local);
            selected.setOnline(online);
            if (compDao.update(selected)) {
                compListView.refresh();
            } else {
                new Alert(Alert.AlertType.ERROR, "Erro ao atualizar compromisso").showAndWait();
            }
        }
    }

    @FXML
    void onDeleteComp() {
        Compromisso selected = compListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confirma exclusão do compromisso em " + selected + "?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                if (compDao.delete(selected.getId())) {
                    compromissos.remove(selected);
                    clearCompForm();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Erro ao excluir compromisso").showAndWait();
                }
            }
        });
    }
}
