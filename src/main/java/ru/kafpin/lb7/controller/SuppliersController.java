package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.kafpin.lb7.App;
import ru.kafpin.lb7.dao.SupplierDao;
import ru.kafpin.lb7.model.Supplier;
import java.util.List;
import java.util.Optional;

public class SuppliersController {

    @FXML private TableView<Supplier> suppliersTable;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colContact;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colEmail;

    private final SupplierDao supplierDao;

    public SuppliersController(SupplierDao supplierDao) {
        this.supplierDao = supplierDao;
    }

    @FXML
    private void initialize() {
        colName.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getName()));
        colContact.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getContactPerson()));
        colPhone.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getPhone()));
        colEmail.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getEmail()));
        refresh();
    }

    @FXML
    private void addSupplier() {
        Dialog<Supplier> dialog = createSupplierDialog(null);
        Optional<Supplier> result = dialog.showAndWait();
        result.ifPresent(s -> {
            try {
                supplierDao.create(s);
                refresh();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка создания: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    private void editSupplier() {
        Supplier selected = suppliersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите поставщика").showAndWait();
            return;
        }
        Dialog<Supplier> dialog = createSupplierDialog(selected);
        Optional<Supplier> result = dialog.showAndWait();
        result.ifPresent(s -> {
            selected.setName(s.getName());
            selected.setContactPerson(s.getContactPerson());
            selected.setPhone(s.getPhone());
            selected.setEmail(s.getEmail());
            selected.setAddress(s.getAddress());
            try {
                supplierDao.update(selected);
                refresh();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка обновления: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    private void deleteSupplier() {
        Supplier selected = suppliersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите поставщика").showAndWait();
            return;
        }
        try {
            supplierDao.delete(selected.getSupplierId());
            refresh();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка удаления: " + e.getMessage()).showAndWait();
        }
    }

    private void refresh() {
        try {
            List<Supplier> suppliers = supplierDao.findAll();
            suppliersTable.setItems(FXCollections.observableArrayList(suppliers));
        } catch (Exception e) {
            System.err.println("Ошибка загрузки поставщиков: " + e.getMessage());
            suppliersTable.setItems(FXCollections.observableArrayList());
        }
    }

    private Dialog<Supplier> createSupplierDialog(Supplier existing) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новый поставщик" : "Редактирование поставщика");
        dialog.setHeaderText("Введите данные поставщика");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(
                App.bundle.getString("button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        TextField contactField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField addressField = new TextField();

        if (existing != null) {
            nameField.setText(existing.getName());
            contactField.setText(existing.getContactPerson());
            phoneField.setText(existing.getPhone());
            emailField.setText(existing.getEmail());
            addressField.setText(existing.getAddress());
        }

        grid.add(new Label("Наименование:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Контактное лицо:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("Телефон:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Адрес:"), 0, 4);
        grid.add(addressField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Supplier s = new Supplier();
                s.setName(nameField.getText().trim());
                s.setContactPerson(contactField.getText().trim());
                s.setPhone(phoneField.getText().trim());
                s.setEmail(emailField.getText().trim());
                s.setAddress(addressField.getText().trim());
                return s;
            }
            return null;
        });
        return dialog;
    }
}