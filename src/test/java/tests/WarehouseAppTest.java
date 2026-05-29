package ru.kafpin.lb7.tests;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class WarehouseAppTest extends ApplicationTest {

    private static final String VALID_USER = "postgres";
    private static final String VALID_PASSWORD = "123";

    private static final int SHORT_PAUSE = 300;
    private static final int MEDIUM_PAUSE = 600;
    private static final int LONG_PAUSE = 1200;

    @Override
    public void start(Stage stage) throws Exception {
        new ru.kafpin.lb7.App().start(stage);
    }

    private void loginAsValidUser() throws TimeoutException {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#usernameField").tryQuery().isPresent());
        clickOn("#usernameField").write(VALID_USER);
        clickOn("#passwordField").write(VALID_PASSWORD);
        clickOn("Войти");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#contentPane").tryQuery().isPresent());
        sleep(MEDIUM_PAUSE);
    }

    private void selectFromNonEditableCombo(String comboBoxId, String itemText) throws TimeoutException {
        clickOn(comboBoxId);
        sleep(SHORT_PAUSE);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () ->
                lookup(itemText).tryQuery().isPresent());
        clickOn(itemText);
        sleep(SHORT_PAUSE);
    }

    private void typeIntoEditableCombo(String comboBoxId, String text) {
        clickOn(comboBoxId).write(text);
        sleep(SHORT_PAUSE);
        press(KeyCode.ENTER);
        release(KeyCode.ENTER);
        sleep(SHORT_PAUSE);
    }

    private void selectTableRowBySubstring(TableView<?> tableView, String substring) {
        int idx = -1;
        int i = 0;
        for (Object item : tableView.getItems()) {
            if (item.toString().contains(substring)) {
                idx = i;
                break;
            }
            i++;
        }
        if (idx == -1) throw new IllegalStateException("Строка не найдена: " + substring);
        final int target = idx;
        interact(() -> tableView.scrollTo(target));
        sleep(SHORT_PAUSE);
        clickOn(substring);
    }

    // =============================================
    // ПОЗИТИВНЫЕ ТЕСТЫ
    // =============================================

    @Test
    @DisplayName("1. Успешная аутентификация")
    void testSuccessfulLogin() throws TimeoutException {
        loginAsValidUser();
        StackPane contentPane = lookup("#contentPane").query();
        assertThat(contentPane.isVisible()).isTrue();
    }

    @Test
    @DisplayName("2. Таблица товаров не пуста")
    void testProductsTableNotEmpty() throws TimeoutException {
        loginAsValidUser();
        clickOn("Справочники").clickOn("Товары");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#productsTable").tryQuery().isPresent());
        TableView<?> tableView = lookup("#productsTable").query();
        assertThat(tableView.getItems()).isNotEmpty();
        sleep(LONG_PAUSE);
    }

    @Test
    @DisplayName("3. Добавление товара")
    void testAddProduct() throws TimeoutException {
        loginAsValidUser();
        clickOn("Справочники").clickOn("Товары");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#productsTable").tryQuery().isPresent());
        clickOn("Добавить");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".text-field").tryQuery().isPresent());
        clickOn(".text-field").write("ТестТовар,ART-TEST,шт,описание,5");
        clickOn("OK");
        sleep(MEDIUM_PAUSE);

        TableView<?> tableView = lookup("#productsTable").query();
        boolean found = tableView.getItems().stream()
                .anyMatch(item -> item.toString().contains("ART-TEST"));
        assertThat(found).isTrue();

        selectTableRowBySubstring(tableView, "ART-TEST");
        clickOn("Удалить");
        sleep(SHORT_PAUSE);
    }

    @Test
    @DisplayName("4. Поиск товара по артикулу")
    void testSearchProduct() throws TimeoutException {
        loginAsValidUser();
        clickOn("Справочники").clickOn("Товары");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#productsTable").tryQuery().isPresent());
        clickOn("#searchField").write("KIR-001");
        clickOn("Найти");
        sleep(MEDIUM_PAUSE);

        TableView<?> tableView = lookup("#productsTable").query();
        boolean allMatch = tableView.getItems().stream()
                .allMatch(item -> item.toString().contains("KIR-001"));
        assertThat(allMatch).isTrue();

        clickOn("Сбросить");
        sleep(SHORT_PAUSE);
    }

    @Test
    @DisplayName("5. Приход товара")
    void testReceipt() throws TimeoutException {
        loginAsValidUser();
        clickOn("Операции").clickOn("Приход");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#supplierCombo").tryQuery().isPresent());
        sleep(SHORT_PAUSE);

        // Выбираем первого поставщика
        clickOn("#supplierCombo");
        sleep(SHORT_PAUSE);
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        sleep(SHORT_PAUSE);

        clickOn("Добавить позицию");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".text-field").tryQuery().isPresent());
        clickOn(".text-field").write("1,10,100.00,1");
        clickOn("OK");
        sleep(SHORT_PAUSE);

        clickOn("Сохранить приход");
        sleep(LONG_PAUSE);

        Label status = lookup("#statusLabel").query();
        assertThat(status.getText()).contains("Готов");
    }

    @Test
    @DisplayName("6. Инвентаризация – формирование списка")
    void testInventoryGenerate() throws TimeoutException {
        loginAsValidUser();
        clickOn("Операции").clickOn("Инвентаризация");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#inventoryTable").tryQuery().isPresent());
        sleep(SHORT_PAUSE);

        clickOn("Сформировать список товаров");
        sleep(LONG_PAUSE);

        TableView<?> invTable = lookup("#inventoryTable").query();
        assertThat(invTable.getItems()).isNotEmpty();

        clickOn("Завершить инвентаризацию");
        sleep(SHORT_PAUSE);
    }

    // =============================================
    // НЕГАТИВНЫЕ ТЕСТЫ
    // =============================================

    @Test
    @DisplayName("7. Неверный пароль")
    void testFailedLogin() throws TimeoutException {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#usernameField").tryQuery().isPresent());
        clickOn("#usernameField").write("wrong");
        clickOn("#passwordField").write("wrong");
        clickOn("Войти");

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".alert").tryQuery().isPresent());
        DialogPane alert = lookup(".alert").query();
        assertThat(alert.getContentText()).contains("Неверный логин или пароль");
        clickOn("OK");
    }

    @Test
    @DisplayName("8. Удаление без выбора")
    void testDeleteWithoutSelection() throws TimeoutException {
        loginAsValidUser();
        clickOn("Справочники").clickOn("Товары");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#productsTable").tryQuery().isPresent());
        clickOn("Удалить");

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".alert").tryQuery().isPresent());
        DialogPane alert = lookup(".alert").query();
        assertThat(alert.getContentText()).contains("Выберите товар");
        clickOn("OK");
    }

    @Test
    @DisplayName("9. Редактирование без выбора")
    void testEditWithoutSelection() throws TimeoutException {
        loginAsValidUser();
        clickOn("Справочники").clickOn("Товары");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#productsTable").tryQuery().isPresent());
        clickOn("Редактировать");

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".alert").tryQuery().isPresent());
        DialogPane alert = lookup(".alert").query();
        assertThat(alert.getContentText()).contains("Выберите товар для редактирования");
        clickOn("OK");
    }

    @Test
    @DisplayName("10. Отгрузка с недостаточным остатком")
    void testShipmentInsufficientStock() throws TimeoutException {
        loginAsValidUser();
        clickOn("Операции").clickOn("Расход");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("#customerField").tryQuery().isPresent());
        sleep(SHORT_PAUSE);

        clickOn("#customerField").write("Тестовый получатель");
        typeIntoEditableCombo("#productCombo", "Песок строительный");

        clickOn("Выполнить отгрузку");
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".text-field").tryQuery().isPresent());

        clickOn(".text-field").write("3,100");
        clickOn("OK");
        sleep(MEDIUM_PAUSE);

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup(".alert").tryQuery().isPresent());
        DialogPane alert = lookup(".alert").query();
        String content = alert.getContentText();
        assertThat(content).contains("Ошибка");
        clickOn("OK");
    }
}