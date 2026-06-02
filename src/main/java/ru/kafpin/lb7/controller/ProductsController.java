package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.kafpin.lb7.dao.ProductDao;
import ru.kafpin.lb7.model.Product;
import java.util.List;

public class ProductsController {

    @FXML private TextField searchField;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colArticle;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colUnit;
    @FXML private TableColumn<Product, Integer> colMinStock;

    private final ProductDao productDao;

    public ProductsController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @FXML
    private void initialize() {
        colArticle.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getArticle()));
        colName.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getName()));
        colUnit.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getUnit()));
        colMinStock.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getMinStock()));

        try {
            refreshTable(productDao.findAll());
        } catch (Exception e) {
            System.err.println("Ошибка загрузки товаров: " + e.getMessage());
        }
    }

    @FXML
    private void search() {
        String keyword = searchField.getText().trim();
        try {
            if (keyword.isEmpty()) {
                refreshTable(productDao.findAll());
            } else {
                refreshTable(productDao.search(keyword));
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка поиска: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void resetSearch() {
        searchField.clear();
        try {
            refreshTable(productDao.findAll());
        } catch (Exception e) {
            System.err.println("Ошибка сброса: " + e.getMessage());
        }
    }

    @FXML
    private void addProduct() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Новый товар");
        dialog.setHeaderText("Введите: название, артикул, ед.изм, описание, мин.остаток через запятую");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length >= 3) {
                Product p = new Product();
                p.setName(parts[0].trim());
                p.setArticle(parts[1].trim());
                p.setUnit(parts[2].trim());
                p.setDescription(parts.length > 3 ? parts[3].trim() : "");
                p.setMinStock(parts.length > 4 ? Integer.parseInt(parts[4].trim()) : 0);
                try {
                    productDao.create(p);
                    refreshTable(productDao.findAll());
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка создания: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void editProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите товар для редактирования").showAndWait();
            return;
        }
        TextInputDialog dialog = new TextInputDialog(
                selected.getName() + "," + selected.getArticle() + "," + selected.getUnit() + ","
                        + selected.getDescription() + "," + selected.getMinStock()
        );
        dialog.setTitle("Редактирование товара");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length >= 3) {
                selected.setName(parts[0].trim());
                selected.setArticle(parts[1].trim());
                selected.setUnit(parts[2].trim());
                selected.setDescription(parts.length > 3 ? parts[3].trim() : "");
                selected.setMinStock(parts.length > 4 ? Integer.parseInt(parts[4].trim()) : 0);
                try {
                    productDao.update(selected);
                    refreshTable(productDao.findAll());
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Ошибка обновления: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    @FXML
    private void deleteProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите товар").showAndWait();
            return;
        }
        try {
            productDao.delete(selected.getProductId());
            refreshTable(productDao.findAll());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка удаления: " + e.getMessage()).showAndWait();
        }
    }

    private void refreshTable(List<Product> list) {
        productsTable.setItems(FXCollections.observableArrayList(list));
    }
}