package com.agenda.ui;

import com.agenda.dao.CompromissoDAO;
import com.agenda.dao.ContatoDAO;
import com.agenda.model.Compromisso;
import com.agenda.model.Contato;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CompromissoController {
    @FXML
    private ComboBox<Contato> filterContatoCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ListView<Compromisso> compListView;
    @FXML
    private ComboBox<Contato> contatoCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeField;
    @FXML
    private TextField localField;
    @FXML
    private TextArea descricaoArea;
    @FXML
    private CheckBox onlineCheck;
    @FXML
    private Button saveCompButton;
    @FXML
    private Button deleteCompButton;

    private final CompromissoDAO compDao = new CompromissoDAO();
    private final ContatoDAO contatoDao = new ContatoDAO();
    private final ObservableList<Compromisso> compromissos = FXCollections.observableArrayList();
    private final ObservableList<Contato> contatos = FXCollections.observableArrayList();
    private FilteredList<Compromisso> filtered;

    @FXML
    public void initialize() {
        // load data
        loadAll();

        // setup filtered list
        filtered = new FilteredList<>(compromissos, p -> true);
        SortedList<Compromisso> sorted = new SortedList<>(filtered);
        compListView.setItems(sorted);

        contatoCombo.setItems(contatos);
        filterContatoCombo.setItems(contatos);

        // cell factory to show date/time + contato name
        compListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Compromisso item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Contato c = findContatoById(item.getContatoId());
                    String nome = c == null ? "<sem contato>" : c.getNome();
                    LocalDateTime dt = item.getDateTime();
                    String dtText = dt == null ? "<sem data>" : dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    String local = item.getLocal();
                    StringBuilder sb = new StringBuilder();
                    sb.append(dtText).append(" - ").append(nome);
                    if (item.isOnline()) sb.append(" (online)");
                    else if (local != null && !local.isEmpty()) sb.append(" - ").append(local);
                    else sb.append(" - (sem local)");
                    setText(sb.toString());
                }
            }
        });

        // listeners for filtering: call updateFilter on change
        startDatePicker.valueProperty().addListener((obs, oldV, newV) -> updateFilter());
        endDatePicker.valueProperty().addListener((obs, oldV, newV) -> updateFilter());
        filterContatoCombo.valueProperty().addListener((obs, oldV, newV) -> updateFilter());

        compListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) showCompromisso(newV);
            else clearForm();
        });
    }

    private void updateFilter() {
        filtered.setPredicate(c -> {
            // read values inside predicate to avoid capturing non-final locals
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            Contato selectedFilterContato = filterContatoCombo.getSelectionModel().getSelectedItem();

            // if both dates provided and start > end, no results
            if (start != null && end != null && start.isAfter(end)) return false;

            // date filter
            LocalDateTime dt = c.getDateTime();
            if (start != null && (dt == null || dt.toLocalDate().isBefore(start))) return false;
            if (end != null && (dt == null || dt.toLocalDate().isAfter(end))) return false;

            // contato filter (if a specific contato selected, only allow matching contatoId)
            if (selectedFilterContato != null) {
                if (c.getContatoId() == null || !selectedFilterContato.getId().equals(c.getContatoId())) return false;
            }

            return true;
        });
    }

    private void loadAll() {
        compromissos.setAll(compDao.findAll());
        contatos.setAll(contatoDao.findAll());
    }

    private void showCompromisso(Compromisso c) {
        if (c == null) return;
        contatoCombo.getSelectionModel().select(findContatoById(c.getContatoId()));
        LocalDateTime dt = c.getDateTime();
        if (dt != null) {
            datePicker.setValue(dt.toLocalDate());
            timeField.setText(dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            datePicker.setValue(null);
            timeField.clear();
        }
        localField.setText(c.getLocal());
        descricaoArea.setText(c.getDescricao() == null ? "" : c.getDescricao());
        onlineCheck.setSelected(c.isOnline());
        deleteCompButton.setDisable(false);
    }

    private Contato findContatoById(Integer id) {
        if (id == null) return null;
        for (Contato c : contatos) if (c.getId() != null && c.getId().equals(id)) return c;
        return null;
    }

    private void clearForm() {
        contatoCombo.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        timeField.clear();
        localField.clear();
        descricaoArea.clear();
        onlineCheck.setSelected(false);
        deleteCompButton.setDisable(true);
    }

    @FXML
    void onNewComp() {
        compListView.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML
    void onExportCompCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar compromissos para CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        chooser.setInitialFileName("compromissos.csv");
        File file = chooser.showSaveDialog(compListView.getScene().getWindow());
        if (file == null) return;
        try {
            com.agenda.util.CompromissoCsvExporter.export(compromissos, contatos, file);
            new Alert(Alert.AlertType.INFORMATION, "Exportado com sucesso: " + file.getAbsolutePath()).showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro ao exportar CSV: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void onSaveComp() {
        Contato contato = contatoCombo.getSelectionModel().getSelectedItem();
        if (contato == null) {
            new Alert(Alert.AlertType.WARNING, "Selecione um contato").showAndWait();
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
            try {
                time = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ex2) {
                new Alert(Alert.AlertType.WARNING, "Hora inválida. Use HH:mm").showAndWait();
                return;
            }
        }

        LocalDateTime dateTime = LocalDateTime.of(date, time);
        String local = localField.getText().trim();
        String descricao = descricaoArea.getText().trim();
        boolean online = onlineCheck.isSelected();

        // Validação adicional: se o compromisso for presencial (online == false), o campo local é obrigatório
        if (!online && local.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Local é obrigatório para compromissos presenciais").showAndWait();
            return;
        }

        Compromisso selected = compListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Compromisso c = new Compromisso(contato.getId(), dateTime, local, online, descricao);
            if (compDao.insert(c)) {
                compromissos.add(c);
                compListView.getSelectionModel().select(c);
            } else {
                new Alert(Alert.AlertType.ERROR, "Erro ao inserir compromisso").showAndWait();
            }
        } else {
            selected.setContatoId(contato.getId());
            selected.setDateTime(dateTime);
            selected.setLocal(local);
            selected.setOnline(online);
            selected.setDescricao(descricao);
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
                    clearForm();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Erro ao excluir compromisso").showAndWait();
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
    void onClearFilters() {
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);
        if (filterContatoCombo != null) filterContatoCombo.getSelectionModel().clearSelection();
        if (filtered != null) filtered.setPredicate(p -> true);
    }

}
