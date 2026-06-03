package ru.kafpin.lb7.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.kafpin.lb7.App;
import ru.kafpin.lb7.dao.ProductDao;
import ru.kafpin.lb7.model.Product;
import java.util.List;
import java.util.Optional;

public class ProductsController {

    @FXML private TextField searchField;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colArticle;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colUnit;
    @FXML private TableColumn<Product, Integer> colMinStock;
    @FXML private TableColumn<Product, String> colDescription;

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
        colDescription.setCellValueFactory(cellData ->
                new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getDescription()));

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
        Dialog<Product> dialog = createProductDialog(null);
        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(product -> {
            try {
                productDao.create(product);
                refreshTable(productDao.findAll());
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка создания: " + e.getMessage()).showAndWait();
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
        Dialog<Product> dialog = createProductDialog(selected);
        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(product -> {
            selected.setName(product.getName());
            selected.setArticle(product.getArticle());
            selected.setUnit(product.getUnit());
            selected.setDescription(product.getDescription());
            selected.setMinStock(product.getMinStock());
            try {
                productDao.update(selected);
                refreshTable(productDao.findAll());
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка обновления: " + e.getMessage()).showAndWait();
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

    private Dialog<Product> createProductDialog(Product existing) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ?
                App.bundle.getString("product.dialog.new") :
                App.bundle.getString("product.dialog.edit"));
        dialog.setHeaderText(App.bundle.getString("product.dialog.header"));

        ButtonType okButtonType = new ButtonType(
                App.bundle.getString("button.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(
                App.bundle.getString("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        TextField articleField = new TextField();
        TextField unitField = new TextField();
        TextField descField = new TextField();
        TextField minStockField = new TextField();

        if (existing != null) {
            nameField.setText(existing.getName());
            articleField.setText(existing.getArticle());
            unitField.setText(existing.getUnit());
            descField.setText(existing.getDescription());
            minStockField.setText(String.valueOf(existing.getMinStock()));
        }

        grid.add(new Label(App.bundle.getString("product.field.name")), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label(App.bundle.getString("product.field.article")), 0, 1);
        grid.add(articleField, 1, 1);
        grid.add(new Label(App.bundle.getString("product.field.unit")), 0, 2);
        grid.add(unitField, 1, 2);
        grid.add(new Label(App.bundle.getString("product.field.description")), 0, 3);
        grid.add(descField, 1, 3);
        grid.add(new Label(App.bundle.getString("product.field.minStock")), 0, 4);
        grid.add(minStockField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Product product = new Product();
                product.setName(nameField.getText().trim());
                product.setArticle(articleField.getText().trim());
                product.setUnit(unitField.getText().trim());
                product.setDescription(descField.getText().trim());
                try {
                    product.setMinStock(Integer.parseInt(minStockField.getText().trim()));
                } catch (NumberFormatException e) {
                    product.setMinStock(0);
                }
                return product;
            }
            return null;
        });

        return dialog;
    }
}