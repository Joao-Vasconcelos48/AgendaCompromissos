package com.agenda.ui;

import com.agenda.dao.ContatoDAO;
import com.agenda.model.Contato;
import com.agenda.util.CsvExporter;
import com.agenda.validation.EmailValidator;
import com.agenda.validation.PhoneValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ContactsController {
    @FXML
    private ComboBox<Contato> filterContactCombo;
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

    private final ContatoDAO dao = new ContatoDAO();
    private final ObservableList<Contato> contatos = FXCollections.observableArrayList();
    private FilteredList<Contato> filtered;

    @FXML
    public void initialize() {
        listView.setItems(contatos);
        loadAll();

        filterContactCombo.setItems(contatos);

        // setup filtered list
        filtered = new FilteredList<>(contatos, p -> true);
        SortedList<Contato> sorted = new SortedList<>(filtered);
        listView.setItems(sorted);

        filterContactCombo.valueProperty().addListener((obs, oldV, newV) -> {
            filtered.setPredicate(c -> {
                if (newV == null) return true;
                return c.getId() != null && c.getId().equals(newV.getId());
            });
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) showContato(newV);
            else clearForm();
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

        // Check duplicates: same email OR same phone OR (if both empty) same name
        if (isDuplicate(selected, nome, email, telefone)) {
            new Alert(Alert.AlertType.WARNING, "Contato duplicado detectado (email/telefone/nome). Verifique antes de salvar.").showAndWait();
            return;
        }

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

    // Helper: normaliza telefone para apenas dígitos (útil para comparação)
    private String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("\\D+", "").trim();
    }

    // Retorna true se encontrar um contato duplicado (exclui o contato atualmente selecionado quando atualizando)
    private boolean isDuplicate(Contato selected, String nome, String email, String telefone) {
        String emailNorm = (email == null) ? "" : email.trim().toLowerCase();
        String phoneNorm = normalizePhone(telefone);
        String nameNorm = (nome == null) ? "" : nome.trim().toLowerCase();

        for (Contato c : contatos) {
            // skip same record when updating
            if (selected != null && c.getId() != null && selected.getId() != null && c.getId().equals(selected.getId())) continue;

            // same email (when provided)
            if (!emailNorm.isEmpty() && c.getEmail() != null && emailNorm.equals(c.getEmail().trim().toLowerCase())) return true;
            // same telefone (when provided) - compare only digits
            if (!phoneNorm.isEmpty() && c.getTelefone() != null && phoneNorm.equals(normalizePhone(c.getTelefone()))) return true;
            // if both email and phone empty, fallback to same name
            if (emailNorm.isEmpty() && phoneNorm.isEmpty() && !nameNorm.isEmpty() && c.getNome() != null && nameNorm.equals(c.getNome().trim().toLowerCase())) return true;
        }
        return false;
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

    @FXML
    void onBackToMenu(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
        Scene scene = new Scene(loader.load(), 700, 450);
        stage.setTitle("Agenda - Menu");
        stage.setScene(scene);
    }

    @FXML
    void onClearFilter() {
        if (filterContactCombo != null) filterContactCombo.getSelectionModel().clearSelection();
        if (filtered != null) filtered.setPredicate(p -> true);
    }
}
