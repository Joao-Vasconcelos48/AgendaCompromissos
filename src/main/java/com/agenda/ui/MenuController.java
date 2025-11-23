package com.agenda.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {
    @FXML
    void onOpenContatos(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/contacts.fxml"));
        Scene scene = new Scene(loader.load(), 900, 500);
        stage.setTitle("Agenda - Contatos");
        stage.setScene(scene);
    }

    @FXML
    void onOpenCompromissos(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/comps.fxml"));
        Scene scene = new Scene(loader.load(), 900, 500);
        stage.setTitle("Agenda - Compromissos");
        stage.setScene(scene);
    }
}

