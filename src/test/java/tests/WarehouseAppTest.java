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

    private static final String VALID_USER = "manager";
    private static final String VALID_PASSWORD = "manager";

    private static final int SHORT_PAUSE = 600;
    private static final int MEDIUM_PAUSE = 1200;

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

    // =============================================
    // ПОЗИТИВНЫЕ ТЕСТЫ
    // =============================================

    @Test
    @DisplayName("1. Добавление нового товара")
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

        // Удаляем тестовый товар
        selectTableRowBySubstring(tableView, "ART-TEST");
        clickOn("Удалить");
        sleep(SHORT_PAUSE);
    }

    @Test
    @DisplayName("2. Приход товара (сохранение накладной)")
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

        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> lookup("Сохранить приход").tryQuery().isPresent());
        clickOn("Сохранить приход");
        sleep(MEDIUM_PAUSE);

        Label status = lookup("#statusLabel").query();
        assertThat(status.getText()).contains("Готов");
    }

    // =============================================
    // НЕГАТИВНЫЕ ТЕСТЫ
    // =============================================

    @Test
    @DisplayName("3. Ошибка при неверном пароле")
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
    @DisplayName("4. Предупреждение при удалении без выбора строки")
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
}