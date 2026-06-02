package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.kafpin.lb7.dao.SupplierDao;
import ru.kafpin.lb7.model.Supplier;
import java.util.List;

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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Новый поставщик");
        dialog.setHeaderText("Введите: наименование, контактное лицо, телефон, email, адрес через запятую");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length >= 1) {
                Supplier s = new Supplier();
                s.setName(parts[0].trim());
                s.setContactPerson(parts.length > 1 ? parts[1].trim() : "");
                s.setPhone(parts.length > 2 ? parts[2].trim() : "");
                s.setEmail(parts.length > 3 ? parts[3].trim() : "");
                s.setAddress(parts.length > 4 ? parts[4].trim() : "");
                try {
                    supplierDao.create(s);
                    refresh();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка создания: " + e.getMessage()).showAndWait();
                }
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
        TextInputDialog dialog = new TextInputDialog(
                selected.getName() + "," + selected.getContactPerson() + "," + selected.getPhone() + ","
                        + selected.getEmail() + "," + selected.getAddress()
        );
        dialog.setTitle("Редактирование поставщика");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length >= 1) {
                selected.setName(parts[0].trim());
                selected.setContactPerson(parts.length > 1 ? parts[1].trim() : "");
                selected.setPhone(parts.length > 2 ? parts[2].trim() : "");
                selected.setEmail(parts.length > 3 ? parts[3].trim() : "");
                selected.setAddress(parts.length > 4 ? parts[4].trim() : "");
                try {
                    supplierDao.update(selected);
                    refresh();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка обновления: " + e.getMessage()).showAndWait();
                }
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
}